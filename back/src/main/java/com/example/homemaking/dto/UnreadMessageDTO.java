package com.example.homemaking.dto;

import lombok.Data;

@Data
public class UnreadMessageDTO {
    private String roomId;
    private String roomName;
    private String orderId;
    private Integer roomType;
    private String lastMsg;
    private String time;
    private Integer unreadCount;
}