package com.example.homemaking.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HomemakingPackageDTO {
    private Long id;
    private LocalDateTime expireStartTime;
    private LocalDateTime expireEndTime;

    private Integer expireStatus;//有效期类型：0永久上架 1限时上架

    private String packageName;


    private BigDecimal packagePrice;


    private String packageDesc;//套餐简介、包含服务内容


    private String packageImg;


    private String unitText;//计价单位：/小时 /次 /台 /月


    private Integer status;//状态：0上架 1下架


    private Integer sort;//排序权重,数值越大，排的越靠前
}