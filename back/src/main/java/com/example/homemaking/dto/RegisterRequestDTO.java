package com.example.homemaking.dto;

import lombok.Data;

import java.util.List;

@Data
public class RegisterRequestDTO {
    private String username;
    private String avatar;
    private String phone;
    private String code;
    private String account;
    private String password;
    private List<String> skills;
    private String gender;
    private String confirmPassword;
    private String role; // 001=普通用户, 002=家政人员
    private String status;

}
