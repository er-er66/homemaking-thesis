package com.example.homemaking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {

    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 下单用户账号
     */
    private String userAccount;

    private String coverUrl;//封面图片地址

    /**
     * 接单家政人员账号，未接单传空字符串/不传
     */
    private String staffAccount;

    /**
     * 服务项目名称
     */
    private String serviceItem;

    /**
     * 上门服务完整地址
     */
    private String serviceAddress;

    /**
     * 预约服务时间
     */
    private String serviceTime;

    /**
     * 派单状态：0待派单 1已派单
     */
    private Integer dispatchStatus;
    /**
     * 派单管理员账号
     */
    private String dispatchAdminAccount;
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
     * 支付密码
     */
    private String payPassword;

    /**
     * 备注（前端额外传remark，数据库无此字段，仅前端提交）
     */
    private String remark;

    private String username;

    /**
     * 下单选择的套餐明细（推荐写法）：
     * {@code [{"packageId":5,"packageNum":2}]}
     * <p>与 {@link #packageIds} 同时存在时以本字段为准。</p>
     */
    private List<OrderPackageItemDTO> packages;

    /**
     * 下单选择的套餐ID（简写），每个套餐数量默认 1。
     * <p>仅当 {@link #packages} 为空时生效。</p>
     */
    private List<Long> packageIds;
}