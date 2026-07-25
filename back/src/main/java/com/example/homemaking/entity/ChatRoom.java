package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoom {
    private String roomId;
    private String roomName;
    private String orderId;
    private Integer roomType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}