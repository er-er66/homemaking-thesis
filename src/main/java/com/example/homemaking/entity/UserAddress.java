package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAddress {

    /**
     * 用户账号ID
     */
    private Long id;

    private Integer userAccount;

    /**
     * 关联用户/员工ID，对应sys_user.id或sys_staff.id
     */

    private Long targetId;

    /**
     * 地址标签
     */
    private String label;
    /**
     * 收货人姓名
     */

    private String consigneeName;

    /**
     * 收货人手机号
     */

    private String consigneePhone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址（街道、小区、门牌号）
     */

    private String detailAddress;

    /**
     * 是否默认地址：0-否 1-是
     */

    private Integer isDefault;

    /**
     * 创建时间
     */

    private LocalDateTime createTime;

    /**
     * 更新时间
     */

    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */

    private Integer isDeleted;
}
