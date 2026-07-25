package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageRead {
    private Integer id;
    private Integer msgId;//消息id
    private String roomId;//房间id
    private String userId;//用户id
    private Integer isRead;//0未读 1已读
    private LocalDateTime readTime;
    private LocalDateTime createdAt;
}