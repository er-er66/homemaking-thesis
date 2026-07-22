package com.example.homemaking.mapper;

import com.example.homemaking.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMapper {

    List<ChatMessage> getHistory(String senderId, String receiverId);

    int insert(ChatMessage message);

    List<Map<String, Object>> getUnreadMessagesByReceiver(String userId);


    List<String> getSysStaffList(String userId);

    int batchInsert(List<ChatMessage> messages);
}