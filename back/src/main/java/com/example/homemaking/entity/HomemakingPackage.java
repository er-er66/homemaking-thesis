package com.example.homemaking.entity;

import lombok.Data;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 家政服务套餐表
 */
@Data

public class HomemakingPackage implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long id;


    private String packageName;


    private BigDecimal packagePrice;


    private String packageDesc;//套餐简介、包含服务内容


    private String packageImg;


    private String unitText;//计价单位：/小时 /次 /台 /月


    private Integer status;//状态：0上架 1下架


    private Integer sort;//排序权重,数值越大，排的越靠前


    private Integer expireStatus;//有效期类型：0永久上架 1限时上架


    private LocalDateTime expireStartTime;//套餐上架起始时间

    private LocalDateTime expireEndTime;//套餐到期结束时间，expire_status=1时生效


    private LocalDateTime createTime;


    private LocalDateTime updateTime;


    private Integer isDeleted;//逻辑删除：0正常 1已删除

}
