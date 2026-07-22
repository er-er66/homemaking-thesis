package com.example.homemaking.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private  long id;

    private String roomId;

    private String senderId;

    private String receiverId;

    private String content;

    //消息类型:text/image/file
    private String msgType;

    private String attachUrl;

    //是否已读  0未读 1已读
    private String isRead;

    //创建时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
