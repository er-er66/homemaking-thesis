package com.example.homemaking.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String account;
    private String phone;
    private String password;
    private String role; // 001=普通用户, 002=家政人员 000=管理员

}
