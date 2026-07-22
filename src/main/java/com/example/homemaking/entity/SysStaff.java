package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class SysStaff {
    private Long id;
    private String account;
    private String age;
    private String workExperience;//工作经验
    private String score;//满分5.0
    private String phone;
    private String password;
    private String username;
    private Integer gender;//0-男 1-女
    private String skills;
    private String avatar;
    private String businessStatus;//营业状态：1-营业中 0-休息不营业
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
