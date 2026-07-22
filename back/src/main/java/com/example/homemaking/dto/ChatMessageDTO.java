package com.example.homemaking.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {


    private String senderId;//发送者id

    private String receiverId;//接收者id

    private String content;

    //消息类型:text/image/file
    private String msgType;

    private String attachUrl;




}
