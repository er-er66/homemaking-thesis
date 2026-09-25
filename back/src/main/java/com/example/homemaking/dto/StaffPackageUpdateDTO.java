package com.example.homemaking.dto;

import lombok.Data;

import java.util.List;

/**
 * 修改家政人员可服务套餐的入参
 */
@Data
public class StaffPackageUpdateDTO {

    /**
     * 新的套餐ID集合，传空数组表示清空该员工的所有套餐绑定
     */
    private List<Long> packageIds;
}
