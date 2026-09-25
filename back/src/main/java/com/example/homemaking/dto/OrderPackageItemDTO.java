package com.example.homemaking.dto;

import lombok.Data;

/**
 * 下单时选择的套餐明细
 */
@Data
public class OrderPackageItemDTO {

    /**
     * 服务套餐ID
     */
    private Long packageId;

    /**
     * 购买数量，不传按 1 处理
     */
    private Integer packageNum;
}
