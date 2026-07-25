package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Integer id;
    private String roomId;
    private String senderId;
    private Integer senderType;
    private String content;
    private String msgType;
    private String attachUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}