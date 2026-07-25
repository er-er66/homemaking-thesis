package com.example.homemaking.services;

import com.example.homemaking.dto.ChatMessageDTO;
import com.example.homemaking.dto.ChatRoomDTO;
import com.example.homemaking.dto.UnreadMessageDTO;
import com.example.homemaking.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    List<ChatMessage> getHistory(String roomId);

    int sendMessage(ChatMessageDTO messageDTO);

    List<UnreadMessageDTO> getUnreadMessages(String userId);

    List<String> getSysStaffList(String userId);

    int markRead(String roomId, String userId);

    List<ChatRoomDTO> getChatRooms(String userId);
}