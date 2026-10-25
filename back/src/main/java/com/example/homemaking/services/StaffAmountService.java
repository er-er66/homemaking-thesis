package com.example.homemaking.services;

import com.example.homemaking.dto.PageResult;
import com.example.homemaking.entity.SysStaffAmount;
import com.example.homemaking.entity.SysStaffAmountRecord;

import java.math.BigDecimal;

/**
 * 家政人员资金账户：收入记账、结算、查询
 * <p>口径约定：</p>
 * <ul>
 *   <li>员工收入 = 订单金额 × 配置比例（{@code staff.income.ratio}）</li>
 *   <li>订单完成 → 记入 {@code pending_amount}（待结算）</li>
 *   <li>完成满 {@code staff.settle.delay-days} 天后，定时任务把 pending 转 settle</li>
 *   <li>{@code total_income} 只增不减；{@code withdraw_amount} 本期不做提现，恒为 0</li>
 * </ul>
 */
public interface StaffAmountService {

    /**
     * 订单完成时记一笔收入。
     * <p>幂等：同一订单同一员工重复调用不会重复加钱，返回 0。</p>
     *
     * @param orderNo      订单编号
     * @param staffAccount 家政人员工号
     * @return 本次记账金额；已记过 / 工号无效时返回 0
     */
    BigDecimal recordOrderIncome(String orderNo, String staffAccount);

    /**
     * 执行一次到期结算（定时任务与手动触发共用）
     *
     * @param delayDays 覆盖配置的延迟天数；null 表示用 {@code staff.settle.delay-days}
     * @return 本次成功结算的流水条数
     */
    int settlePending(Integer delayDays);

    /**
     * 按家政人员ID查资金账户
     */
    SysStaffAmount getByStaffId(Long staffId);

    /**
     * 按家政人员工号查资金账户，查不到返回 null
     */
    SysStaffAmount getByStaffAccount(String staffAccount);

    /**
     * 资金账户分页；不传 pageNum/pageSize 时返回全量（沿用项目双模式约定）
     */
    PageResult<SysStaffAmount> pageAmount(String staffAccount, String staffName,
                                          Integer pageNum, Integer pageSize);

    /**
     * 资金流水分页；不传分页参数时返回全量
     */
    PageResult<SysStaffAmountRecord> pageRecords(Long staffId, String staffAccount,
                                                 Integer settleStatus, Integer recordType,
                                                 Integer pageNum, Integer pageSize);

    /**
     * 当前生效的分成比例，供前端提示文案使用
     */
    BigDecimal getIncomeRatio();

    /**
     * 当前生效的结算延迟天数
     */
    int getSettleDelayDays();
}
