package com.example.homemaking.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 员工-套餐关联的轻量视图，用于批量查询后按员工分组展示，避免 N+1
 */
@Data
public class StaffPackageRefDTO {

    /**
     * 家政人员账号
     */
    private String staffAccount;

    private Long packageId;

    private String packageName;

    private BigDecimal packagePrice;

    private String unitText;
}
