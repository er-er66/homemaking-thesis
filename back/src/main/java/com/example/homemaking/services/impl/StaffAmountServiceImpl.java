package com.example.homemaking.services.impl;

import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.Order;
import com.example.homemaking.entity.SysStaffAmount;
import com.example.homemaking.entity.SysStaffAmountRecord;
import com.example.homemaking.mapper.SysStaffAmountMapper;
import com.example.homemaking.mapper.SysStaffAmountRecordMapper;
import com.example.homemaking.services.StaffAmountService;
import com.example.homemaking.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class StaffAmountServiceImpl implements StaffAmountService {

    /** 结算批大小：一次最多锁 200 条流水，避免长事务 */
    private static final int SETTLE_BATCH_SIZE = 200;

    /**
     * 员工分成比例。改这里即可调整口径，历史流水已存快照，不受影响。
     */
    @Value("${staff.income.ratio:0.8}")
    private BigDecimal incomeRatio;

    /**
     * 订单完成到可结算的延迟天数，给售后留缓冲
     */
    @Value("${staff.settle.delay-days:7}")
    private int settleDelayDays;

    @Autowired
    private SysStaffAmountMapper amountMapper;

    @Autowired
    private SysStaffAmountRecordMapper recordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal recordOrderIncome(String orderNo, String staffAccount) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            log.warn("记账跳过：orderNo 为空");
            return BigDecimal.ZERO;
        }
        // 下单时 staff_account 会被写成字面量 "null"，完成订单前必须已接单，这里做防御
        if (staffAccount == null || staffAccount.trim().isEmpty()
                || "null".equalsIgnoreCase(staffAccount.trim())) {
            log.warn("记账跳过：订单 {} 没有有效的家政人员账号（staffAccount={}）", orderNo, staffAccount);
            return BigDecimal.ZERO;
        }

        Order order = amountMapper.selectOrderByNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("记账失败：订单不存在，orderNo=" + orderNo);
        }
        if (order.getOrderAmount() == null || order.getOrderAmount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("记账跳过：订单 {} 金额无效（{}）", orderNo, order.getOrderAmount());
            return BigDecimal.ZERO;
        }

        Long staffId = amountMapper.selectStaffIdByAccount(staffAccount);
        if (staffId == null) {
            throw new IllegalArgumentException("记账失败：家政人员不存在，account=" + staffAccount);
        }

        BigDecimal income = order.getOrderAmount()
                .multiply(incomeRatio)
                .setScale(2, RoundingMode.HALF_UP);
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("记账跳过：订单 {} 计提收入为 0（金额={}, 比例={}）",
                    orderNo, order.getOrderAmount(), incomeRatio);
            return BigDecimal.ZERO;
        }

        SysStaffAmountRecord record = new SysStaffAmountRecord();
        record.setStaffId(staffId);
        record.setStaffAccount(staffAccount);
        record.setOrderNo(orderNo);
        record.setOrderAmount(order.getOrderAmount());
        record.setRatio(incomeRatio.setScale(3, RoundingMode.HALF_UP));
        record.setIncomeAmount(income);
        record.setRecordType(SysStaffAmountRecord.TYPE_ORDER_INCOME);
        record.setSettleStatus(SysStaffAmountRecord.SETTLE_PENDING);
        record.setRemark("订单完成自动计提，比例 " + incomeRatio);

        // 幂等判断不能靠 insert 的返回值：MySQL Connector/J 默认 useAffectedRows=false，
        // JDBC 返回 found rows，唯一键冲突时同样返回 1，会重复加钱。
        // 这里先查流水，再靠 uk_order_staff_type 兜住并发。
        SysStaffAmountRecord existing = recordMapper.selectByOrder(orderNo, staffId);
        if (existing != null) {
            log.info("订单已记过账，跳过：orderNo={}, staffId={}, 已记金额={}",
                    orderNo, staffId, existing.getIncomeAmount());
            return BigDecimal.ZERO;
        }

        try {
            recordMapper.insertRecord(record);
        } catch (DuplicateKeyException e) {
            // 并发场景：另一个线程刚插了同一条流水，本次不再累加
            log.info("并发重复记账，跳过：orderNo={}, staffId={}", orderNo, staffId);
            return BigDecimal.ZERO;
        }

        int affected = amountMapper.upsertIncome(staffId, income);
        if (affected <= 0) {
            throw new IllegalStateException("记账失败：资金账户更新失败，staffId=" + staffId);
        }

        log.info("记账成功：orderNo={}, staffId={}, 订单金额={}, 比例={}, 收入={}",
                orderNo, staffId, order.getOrderAmount(), incomeRatio, income);
        return income;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int settlePending(Integer delayDays) {
        int days = (delayDays == null || delayDays < 0) ? settleDelayDays : delayDays;
        LocalDateTime before = LocalDateTime.now().minusDays(days);
        int total = 0;

        while (true) {
            List<SysStaffAmountRecord> pending = recordMapper.selectPending(before, SETTLE_BATCH_SIZE);
            if (pending.isEmpty()) {
                break;
            }
            int handled = 0;
            for (SysStaffAmountRecord record : pending) {
                // 先抢占标记：受影响行数 > 0 才说明本次归我结算，防止并发重复加钱
                int marked = recordMapper.markSettled(record.getId(), LocalDateTime.now());
                if (marked <= 0) {
                    continue;
                }
                int affected = amountMapper.addSettled(record.getStaffId(), record.getIncomeAmount());
                if (affected <= 0) {
                    // 抛异常触发整批回滚：标记已改但钱没动，会造成账实不符
                    throw new IllegalStateException(
                            "结算失败：资金账户不存在，staffId=" + record.getStaffId());
                }
                handled++;
            }
            total += handled;
            // 手头不足一批说明已经扫完；本批一条都没处理成功则退出，避免死循环
            if (pending.size() < SETTLE_BATCH_SIZE || handled == 0) {
                break;
            }
        }

        log.info("资金结算完成：delayDays={}, 结算 {} 笔", days, total);
        return total;
    }

    @Override
    public SysStaffAmount getByStaffId(Long staffId) {
        if (staffId == null) {
            return null;
        }
        return amountMapper.selectByStaffId(staffId);
    }

    @Override
    public SysStaffAmount getByStaffAccount(String staffAccount) {
        if (staffAccount == null || staffAccount.trim().isEmpty()) {
            return null;
        }
        return amountMapper.selectByStaffAccount(staffAccount);
    }

    @Override
    public PageResult<SysStaffAmount> pageAmount(String staffAccount, String staffName,
                                                 Integer pageNum, Integer pageSize) {
        if (!PageUtil.enabled(pageNum, pageSize)) {
            List<SysStaffAmount> all = amountMapper.search(staffAccount, staffName, null, null);
            return PageResult.of(all.size(), 1, all.size(), all);
        }
        int num = PageUtil.normalizePageNum(pageNum);
        int size = PageUtil.normalizePageSize(pageSize);
        long total = amountMapper.count(staffAccount, staffName);
        List<SysStaffAmount> records = total == 0 ? List.of()
                : amountMapper.search(staffAccount, staffName, PageUtil.offset(num, size), size);
        return PageResult.of(total, num, size, records);
    }

    @Override
    public PageResult<SysStaffAmountRecord> pageRecords(Long staffId, String staffAccount,
                                                        Integer settleStatus, Integer recordType,
                                                        Integer pageNum, Integer pageSize) {
        if (!PageUtil.enabled(pageNum, pageSize)) {
            List<SysStaffAmountRecord> all =
                    recordMapper.searchRecords(staffId, staffAccount, settleStatus, recordType, null, null);
            return PageResult.of(all.size(), 1, all.size(), all);
        }
        int num = PageUtil.normalizePageNum(pageNum);
        int size = PageUtil.normalizePageSize(pageSize);
        long total = recordMapper.countRecords(staffId, staffAccount, settleStatus, recordType);
        List<SysStaffAmountRecord> records = total == 0 ? List.of()
                : recordMapper.searchRecords(staffId, staffAccount, settleStatus, recordType,
                PageUtil.offset(num, size), size);
        return PageResult.of(total, num, size, records);
    }

    @Override
    public BigDecimal getIncomeRatio() {
        return incomeRatio;
    }

    @Override
    public int getSettleDelayDays() {
        return settleDelayDays;
    }
}
