// UnreadMessageDTO.java
package com.example.homemaking.dto;

import lombok.Data;

@Data
public class UnreadMessageDTO {
    private String merchantId;
    private String merchantName;
    private String merchantAvatar;
    private String lastMsg;
    private String time;
    private Integer unreadCount;



}