package com.example.homemaking.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单-服务套餐关联表（多对多中间表）
 * <p>唯一约束 uk_order_package(order_no, package_id)，同一订单同一套餐不可重复</p>
 */
@Data
public class HomemakingOrderPackage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单编号，关联 homemaking_order.order_no
     */
    private String orderNo;

    /**
     * 服务套餐ID，关联 homemaking_package.id
     */
    private Long packageId;

    /**
     * 套餐购买数量
     */
    private Integer packageNum;

    /**
     * 下单时套餐单价（价格快照，防止后续调价影响历史订单）
     */
    private BigDecimal unitPrice;

    private LocalDateTime createTime;

    // ---------- 以下为 join homemaking_package 带出的展示字段，不是本表列 ----------

    /**
     * 套餐名称（join 带出）
     */
    private String packageName;

    /**
     * 计价单位（join 带出）
     */
    private String unitText;

    /**
     * 套餐封面（join 带出）
     */
    private String packageImg;
}
