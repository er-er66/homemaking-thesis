package com.example.homemaking.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class Order {
    /**
     * 订单主键ID
     */

    private Long id;

    /**
     * 订单编号（唯一）
     */

    private String orderNo;

    /**
     * 下单用户账号，关联sys_user.account
     */

    private String userAccount;

    /**
     * 接单家政人员账号，关联sys_staff.account
     */

    private String staffAccount;

    /**
     * 服务项目名称
     */

    private String serviceItem;

    /**
     * 上门服务地址
     */

    private String serviceAddress;

    /**
     * 预约服务时间
     */

    private String serviceTime;

    /**
     * 订单金额
     */

    private BigDecimal orderAmount;

    /**
     * 订单状态：0待接单 1已接单 2服务完成 3已取消
     */

    private Integer orderStatus;

    /**
     * 支付状态：0未支付 1已支付
     */

    private Integer payStatus;
    /**
     * 派单状态：0待派单 1已派单
     */
    private Integer dispatchStatus;
    /**
     * 派单管理员账号
     */
    private String dispatchAdminAccount;
    /**
     * 支付时验证的支付密码
     */

    private String payPassword;
    /**
     * 下单时间
     */
    private LocalDateTime createTime;

    /**
     * 订单更新时间
     */

    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0正常 1已删除
     */
    private Integer isDeleted;

    /**
     * 订单封面图片URL(user_img表)专门给前端展示图片特意加的
     */
    private String coverUrl;
}