package com.example.homemaking.vo;

import lombok.Data;

@Data
public class LoginResultVO {
    private String token;
    private String username;
    private String avatar;
    private String roleCode;
    private String account;
    private String phone;
}
