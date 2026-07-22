package com.example.homemaking.dto;

import lombok.Data;

@Data
public class AddressSaveDTO {
    private Long id;
    private Integer userAccount;
    private Long targetId;
    private String label;
    private String consigneeName;
    private String consigneePhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private Integer isDefault;//0-非默认，1-默认
}
