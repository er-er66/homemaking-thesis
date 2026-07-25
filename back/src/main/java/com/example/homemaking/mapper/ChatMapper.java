package com.example.homemaking.mapper;

import com.example.homemaking.entity.ChatMessage;
import com.example.homemaking.entity.ChatMessageRead;
import com.example.homemaking.entity.ChatRoom;
import com.example.homemaking.entity.ChatRoomMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface ChatMapper {

    List<ChatMessage> getHistory(String roomId);

    int insert(ChatMessage message);

    int insertMessageRead(ChatMessageRead read);

    int batchInsertMessageRead(List<ChatMessageRead> readList);

    List<ChatRoomMember> getRoomMembers(String roomId);

    List<ChatRoomMember> getRoomMembersExcludeSender(String roomId, String senderId);

    int updateMessageRead(String roomId, String userId);

    List<Map<String, Object>> getUnreadMessagesByUser(String userId);

    List<String> getSysStaffList(String userId);

    int batchInsert(List<ChatMessage> messages);

    ChatRoom getChatRoomById(String roomId);

    int insertChatRoom(ChatRoom chatRoom);

    int insertChatRoomMember(ChatRoomMember member);

    List<ChatRoom> getChatRoomsByUserId(String userId);

    List<Integer> getUnreadMsgIdsByRoomAndUser(String roomId, String userId);

    List<Map<String, Object>> getChatRoomList(String userId);
}