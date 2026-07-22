package com.example.homemaking.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class HomemakingOrder {
    private Long id;
    private String orderNo;
    private String userAccount;
    private String staffAccount;
    private String serviceItem;
    private String serviceAddress;
    private LocalDateTime serviceTime;
    private BigDecimal orderAmount;
    private Integer orderStatus;
    private Integer payStatus;
    private String payPassword;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
