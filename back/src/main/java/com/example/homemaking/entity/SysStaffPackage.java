package com.example.homemaking.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家政人员-服务套餐关联表（替代 sys_staff.skills 逗号分隔字段）
 */
@Data
public class SysStaffPackage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 家政人员账号，关联 sys_staff.account
     */
    private String staffAccount;

    /**
     * 服务套餐ID，关联 homemaking_package.id
     */
    private Long packageId;

    private LocalDateTime createTime;

    // ---------- 以下为 join homemaking_package 带出的展示字段，不是本表列 ----------

    /**
     * 套餐名称（join 带出）
     */
    private String packageName;

    /**
     * 套餐单价（join 带出）
     */
    private BigDecimal packagePrice;

    /**
     * 计价单位（join 带出）
     */
    private String unitText;
}
