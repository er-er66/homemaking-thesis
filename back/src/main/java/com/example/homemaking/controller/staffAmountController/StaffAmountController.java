package com.example.homemaking.controller.staffAmountController;

import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysStaffAmount;
import com.example.homemaking.entity.SysStaffAmountRecord;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.StaffAmountService;
import com.example.homemaking.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 家政人员资金账户
 * <p>本期不做提现，只维护 total_income / pending_amount / settle_amount。</p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/staff-amount")
public class StaffAmountController {

    @Autowired
    private StaffAmountService staffAmountService;

    /**
     * 资金账户列表
     * <p>不传 pageNum/pageSize 返回全量数组，传则返回 {total,pageNum,pageSize,records}。</p>
     */
    @GetMapping("/list")
    public Result<?> list(@RequestParam(required = false) String staffAccount,
                          @RequestParam(required = false) String staffName,
                          @RequestParam(required = false) Integer pageNum,
                          @RequestParam(required = false) Integer pageSize) {
        log.info("查询资金账户列表，staffAccount={}, staffName={}, pageNum={}, pageSize={}",
                staffAccount, staffName, pageNum, pageSize);
        PageResult<SysStaffAmount> page =
                staffAmountService.pageAmount(staffAccount, staffName, pageNum, pageSize);
        if (!PageUtil.enabled(pageNum, pageSize)) {
            return Result.success(page.getRecords());
        }
        return Result.success(page);
    }

    /**
     * 资金流水
     *
     * @param staffId      家政人员ID，可空
     * @param staffAccount 工号模糊匹配，可空
     * @param settleStatus 0待结算 1已结算，可空
     * @param recordType   1订单收入 2结算 3冻结 4解冻，可空
     */
    @GetMapping("/records")
    public Result<?> records(@RequestParam(required = false) Long staffId,
                             @RequestParam(required = false) String staffAccount,
                             @RequestParam(required = false) Integer settleStatus,
                             @RequestParam(required = false) Integer recordType,
                             @RequestParam(required = false) Integer pageNum,
                             @RequestParam(required = false) Integer pageSize) {
        log.info("查询资金流水，staffId={}, staffAccount={}, settleStatus={}, recordType={}, pageNum={}, pageSize={}",
                staffId, staffAccount, settleStatus, recordType, pageNum, pageSize);
        PageResult<SysStaffAmountRecord> page = staffAmountService.pageRecords(
                staffId, staffAccount, settleStatus, recordType, pageNum, pageSize);
        if (!PageUtil.enabled(pageNum, pageSize)) {
            return Result.success(page.getRecords());
        }
        return Result.success(page);
    }

    /**
     * 当前生效的结算配置，前端用来提示「T+N 天自动结算」
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("incomeRatio", staffAmountService.getIncomeRatio());
        map.put("settleDelayDays", staffAmountService.getSettleDelayDays());
        return Result.success(map);
    }

    /**
     * 员工查自己的资金账户（家政端页面用）
     */
    @GetMapping("/my")
    public Result<?> my(@RequestParam String staffAccount) {
        SysStaffAmount amount = staffAmountService.getByStaffAccount(staffAccount);
        if (amount == null) {
            // 没记过账的员工还没建账户，返回全 0 而不是 404，前端不用特殊处理
            SysStaffAmount empty = new SysStaffAmount();
            empty.setStaffAccount(staffAccount);
            empty.setTotalIncome(java.math.BigDecimal.ZERO);
            empty.setPendingAmount(java.math.BigDecimal.ZERO);
            empty.setSettleAmount(java.math.BigDecimal.ZERO);
            empty.setWithdrawAmount(java.math.BigDecimal.ZERO);
            empty.setFreezeAmount(java.math.BigDecimal.ZERO);
            return Result.success(empty);
        }
        return Result.success(amount);
    }

    /**
     * 手动触发一次结算（对账/补跑用）
     *
     * @param delayDays 覆盖配置的延迟天数；传 0 表示立即结算全部待结算流水
     */
    @PostMapping("/settle/run")
    public Result<String> runSettle(@RequestParam(required = false) Integer delayDays) {
        log.info("手动触发资金结算，delayDays={}", delayDays);
        int count = staffAmountService.settlePending(delayDays);
        return Result.success("结算完成，共处理 " + count + " 笔");
    }

    /**
     * 单个员工的资金账户
     */
    @GetMapping("/{staffId}")
    public Result<?> detail(@PathVariable Long staffId) {
        SysStaffAmount amount = staffAmountService.getByStaffId(staffId);
        if (amount == null) {
            return Result.error("该员工还没有资金账户");
        }
        return Result.success(amount);
    }
}
