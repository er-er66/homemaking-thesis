package com.example.homemaking.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家政人员资金流水
 * <p>每一笔收入/结算都留一条记录，供对账与幂等控制。
 * {@code uk_order_staff_type} 保证同一订单同一员工同一类型只记一次。</p>
 */
@Data
public class SysStaffAmountRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流水类型：订单收入 */
    public static final int TYPE_ORDER_INCOME = 1;
    /** 流水类型：结算 */
    public static final int TYPE_SETTLE = 2;
    /** 流水类型：冻结 */
    public static final int TYPE_FREEZE = 3;
    /** 流水类型：解冻 */
    public static final int TYPE_UNFREEZE = 4;

    /** 结算状态：待结算 */
    public static final int SETTLE_PENDING = 0;
    /** 结算状态：已结算 */
    public static final int SETTLE_DONE = 1;

    private Long id;

    private Long staffId;

    private String staffAccount;

    private String orderNo;

    /**
     * 订单原始金额快照
     */
    private BigDecimal orderAmount;

    /**
     * 记账时的分成比例快照（如 0.800），后续调比例不影响历史流水
     */
    private BigDecimal ratio;

    /**
     * 本笔收入 = orderAmount * ratio，已按分四舍五入
     */
    private BigDecimal incomeAmount;

    /**
     * 流水类型：1订单收入 2结算 3冻结 4解冻
     */
    private Integer recordType;

    /**
     * 结算状态：0待结算 1已结算
     */
    private Integer settleStatus;

    private LocalDateTime settleTime;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    /**
     * 非表字段：员工姓名（查询 join 带出）
     */
    private String staffName;
}
