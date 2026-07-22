package com.example.homemaking.entity;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysAdmin {
    private Long id;
    private String username;
    private String phone;
    private String account;
    private String password;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String avatar;//头像
}
