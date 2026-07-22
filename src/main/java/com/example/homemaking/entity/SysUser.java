package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class SysUser {
    private Long id;
    private String account;
    private String phone;
    private String password;
    private String username;
    private String payPassword;
    private String avatar;
    private Integer status;
    private Integer gender;//0-男 1-女
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
