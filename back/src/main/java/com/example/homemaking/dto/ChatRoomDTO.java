package com.example.homemaking.dto;

import lombok.Data;

@Data
public class ChatRoomDTO {
    private String roomId;
    private String name;
    private String account;
    private String avatar;
    private String type;
    private String lastMsg;
    private String lastTime;
    private Integer unread;
}