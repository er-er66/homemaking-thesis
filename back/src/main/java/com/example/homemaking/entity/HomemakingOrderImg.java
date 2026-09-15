package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomemakingOrderImg {

    private Long id;               // '主键ID',
    private String orderNo;       // '订单编号，关联homemaking_order.order_no',
    private String userAccount;     // '下单用户账号',
    private String staffAccount;  // '接单家政人员账号',
    private String imgUrl;        //'图片地址',
    private Integer imgType;      // '图片类型：0打扫前 1打扫后',
    private LocalDateTime finishTime;  //'服务完成时间',
    private LocalDateTime createTime;   //'上传时间',
    private LocalDateTime updateTime;  // '更新时间',
    private Integer isDeleted;         //'逻辑删除：0正常 1已删除',
}
