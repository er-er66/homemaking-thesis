package com.example.homemaking.dto;

import lombok.Data;

@Data
public class HomemakingOrderImgDTO {
    private Integer orderId;
    private String orderNo;
    private String staffAccount;
    private String userAccount;
    private String[] beforeCleanImgs;  // 清理前图片
    private String[] afterCleanImgs; // 清理后的图片
}
