package com.example.homemaking.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String roomId;
    private String senderId;
    private Integer senderType;
    private String content;
    private String msgType;
    private String attachUrl;
}