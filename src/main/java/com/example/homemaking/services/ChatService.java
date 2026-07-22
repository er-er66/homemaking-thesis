package com.example.homemaking.services;

import com.example.homemaking.dto.ChatMessageDTO;
import com.example.homemaking.dto.UnreadMessageDTO;
import com.example.homemaking.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    /**
     * 获取聊天记录
     */

    List<ChatMessage> getHistory(String senderId, String receiverId);


    /**
     * 发送消息
     */
       int sendMessage(ChatMessageDTO messageDTO);

    /**
     * 获取用户的未读消息列表
     */
    List<UnreadMessageDTO> getUnreadMessages(String userId);


    List<String> getSysStaffList(String userId);
}
