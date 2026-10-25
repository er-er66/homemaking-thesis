package com.example.homemaking.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家政人员资金账户（汇总表）
 * <p>一个家政人员只有一条记录，由 uk_staff_id 唯一约束保证。
 * 所有金额变更都应通过 {@link SysStaffAmountRecord} 流水驱动，避免对不上账。</p>
 */
@Data
public class SysStaffAmount implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 家政人员ID，关联 sys_staff.id
     */
    private Long staffId;

    /**
     * 累计总收入：所有已记收入之和，只增不减
     */
    private BigDecimal totalIncome;

    /**
     * 已结算金额
     */
    private BigDecimal settleAmount;

    /**
     * 待结算金额
     */
    private BigDecimal pendingAmount;

    /**
     * 已提现金额（本次需求不做提现流程，恒为 0）
     */
    private BigDecimal withdrawAmount;

    /**
     * 冻结金额（售后纠纷等）
     */
    private BigDecimal freezeAmount;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    /**
     * 非表字段：查询时 join sys_staff 带出，方便前端展示
     */
    private String staffAccount;

    /**
     * 非表字段：家政人员姓名
     */
    private String staffName;
}
