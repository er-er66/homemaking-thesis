package com.example.homemaking.services.impl;

import com.example.homemaking.dto.ChatMessageDTO;
import com.example.homemaking.dto.ChatRoomDTO;
import com.example.homemaking.dto.UnreadMessageDTO;
import com.example.homemaking.entity.ChatMessage;
import com.example.homemaking.entity.ChatMessageRead;
import com.example.homemaking.entity.ChatRoom;
import com.example.homemaking.entity.ChatRoomMember;
import com.example.homemaking.mapper.ChatMapper;
import com.example.homemaking.services.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMapper chatMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String CHAT_QUEUE_KEY = "chat:msg:queue";
    private static final String UNREAD_UID_KEY = "chat:unread:uid:";
    private static final String UNREAD_ROOM_KEY = "chat:unread:room:";

    @Override
    public List<ChatMessage> getHistory(String roomId) {
        return chatMapper.getHistory(roomId);
    }

    @Override
    @Transactional
    public int sendMessage(ChatMessageDTO messageDTO) {
        ChatMessage message = new ChatMessage();
        BeanUtils.copyProperties(messageDTO, message);
        if (message.getSenderType() == null) {
            message.setSenderType(1);
        }
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        ensureRoomAndMember(messageDTO.getRoomId(), messageDTO.getSenderId(), message.getSenderType());

        chatMapper.insert(message);
        Integer msgId = message.getId();
        log.info("消息已插入DB: msgId={}, roomId={}, senderId={}", msgId, messageDTO.getRoomId(), messageDTO.getSenderId());

        List<ChatRoomMember> members = chatMapper.getRoomMembersExcludeSender(
                messageDTO.getRoomId(), messageDTO.getSenderId());

        if (members != null && !members.isEmpty()) {
            List<ChatMessageRead> readList = new ArrayList<>();
            for (ChatRoomMember member : members) {
                ChatMessageRead read = new ChatMessageRead();
                read.setMsgId(msgId);
                read.setRoomId(messageDTO.getRoomId());
                read.setUserId(member.getUserId());
                read.setIsRead(0);
                read.setCreatedAt(LocalDateTime.now());
                readList.add(read);

                stringRedisTemplate.opsForSet().add(UNREAD_UID_KEY + member.getUserId(), msgId.toString());
                String roomUnreadKey = UNREAD_ROOM_KEY + messageDTO.getRoomId() + ":" + member.getUserId();
                try {
                    stringRedisTemplate.opsForValue().increment(roomUnreadKey, 1);
                } catch (Exception e) {
                    stringRedisTemplate.opsForValue().set(roomUnreadKey, "1");
                }
            }
            chatMapper.batchInsertMessageRead(readList);
            log.info("批量插入已读记录 {} 条", readList.size());
        }

        redisTemplate.opsForList().leftPush(CHAT_QUEUE_KEY, message);
        log.info("消息已写入Redis队列: msgId={}, roomId={}", msgId, messageDTO.getRoomId());

        return 1;
    }

    private void ensureRoomAndMember(String roomId, String userId, Integer userType) {
        ChatRoom room = chatMapper.getChatRoomById(roomId);
        if (room == null) {
            room = new ChatRoom();
            room.setRoomId(roomId);
            room.setRoomType(1);
            room.setCreateTime(LocalDateTime.now());
            room.setUpdateTime(LocalDateTime.now());
            chatMapper.insertChatRoom(room);
            log.info("自动创建房间: roomId={}", roomId);
        }

        List<ChatRoomMember> allMembers = chatMapper.getRoomMembers(roomId);
        boolean isMember = allMembers != null && allMembers.stream()
                .anyMatch(m -> m.getUserId().equals(userId));
        if (!isMember) {
            ChatRoomMember member = new ChatRoomMember();
            member.setRoomId(roomId);
            member.setUserId(userId);
            member.setUserType(userType != null ? userType : 1);
            member.setJoinTime(LocalDateTime.now());
            chatMapper.insertChatRoomMember(member);
            log.info("自动添加成员: roomId={}, userId={}", roomId, userId);
        }

        if (allMembers != null && allMembers.size() <= 1 && roomId.contains("_")) {
            String[] parts = roomId.split("_");
            for (String part : parts) {
                if (!part.isEmpty() && !part.equals(userId)
                        && allMembers.stream().noneMatch(m -> m.getUserId().equals(part))) {
                    ChatRoomMember other = new ChatRoomMember();
                    other.setRoomId(roomId);
                    other.setUserId(part);
                    other.setUserType(1);
                    other.setJoinTime(LocalDateTime.now());
                    chatMapper.insertChatRoomMember(other);
                    log.info("自动添加对方成员: roomId={}, userId={}", roomId, part);
                }
            }
        }
    }

    @Override
    public List<UnreadMessageDTO> getUnreadMessages(String userId) {
        List<Map<String, Object>> rawData = chatMapper.getUnreadMessagesByUser(userId);
        List<UnreadMessageDTO> result = new ArrayList<>();

        for (Map<String, Object> item : rawData) {
            String roomId = (String) item.get("roomId");
            UnreadMessageDTO dto = new UnreadMessageDTO();
            dto.setRoomId(roomId);
            dto.setRoomName(item.get("roomName") != null ? item.get("roomName").toString() : "");
            dto.setOrderId(item.get("orderId") != null ? item.get("orderId").toString() : "");
            dto.setRoomType(item.get("roomType") != null ? ((Number) item.get("roomType")).intValue() : 1);
            dto.setLastMsg(item.get("lastMsg") != null ? item.get("lastMsg").toString() : "");

            Object timeObj = item.get("time");
            if (timeObj instanceof Date) {
                dto.setTime(formatDateTime((Date) timeObj));
            } else {
                dto.setTime(timeObj != null ? timeObj.toString() : "");
            }

            Object countObj = item.get("unreadCount");
            dto.setUnreadCount(countObj != null ? ((Number) countObj).intValue() : 0);
            result.add(dto);
        }

        return result;
    }

    @Override
    public List<String> getSysStaffList(String userId) {
        List<String> sysStaffListAll = chatMapper.getSysStaffList(userId);
        List<String> sysStaffList = new ArrayList<>();
        if (sysStaffListAll != null && !sysStaffListAll.isEmpty()) {
            sysStaffList = new ArrayList<>(new HashSet<>(sysStaffListAll));
        } else {
            sysStaffList.add("无聊天记录");
        }
        return sysStaffList;
    }

    @Override
    public int markRead(String roomId, String userId) {
        List<Integer> unreadMsgIds = chatMapper.getUnreadMsgIdsByRoomAndUser(roomId, userId);
        if (unreadMsgIds != null && !unreadMsgIds.isEmpty()) {
            String[] msgIdArr = unreadMsgIds.stream().map(String::valueOf).toArray(String[]::new);
            stringRedisTemplate.opsForSet().remove(UNREAD_UID_KEY + userId, msgIdArr);
        }
        stringRedisTemplate.opsForValue().set(UNREAD_ROOM_KEY + roomId + ":" + userId, "0");

        int count = chatMapper.updateMessageRead(roomId, userId);
        log.info("标记已读: roomId={}, userId={}, 更新 {} 条", roomId, userId, count);
        return count;
    }

    @Override
    public List<ChatRoomDTO> getChatRooms(String userId) {
        List<Map<String, Object>> rawData = chatMapper.getChatRoomList(userId);
        List<ChatRoomDTO> result = new ArrayList<>();
        for (Map<String, Object> item : rawData) {
            ChatRoomDTO dto = new ChatRoomDTO();
            dto.setRoomId((String) item.get("roomId"));
            dto.setName(item.get("name") != null ? item.get("name").toString() : "");
            dto.setAccount(item.get("account") != null ? item.get("account").toString() : "");
            dto.setAvatar(item.get("avatar") != null ? item.get("avatar").toString() : "");
            dto.setType(item.get("type") != null ? item.get("type").toString() : "staff");
            dto.setLastMsg(item.get("lastMsg") != null ? item.get("lastMsg").toString() : "");
            Object timeObj = item.get("lastTime");
            if (timeObj instanceof Date) {
                dto.setLastTime(formatDateTime((Date) timeObj));
            } else {
                dto.setLastTime(timeObj != null ? timeObj.toString() : "");
            }
            Object unreadObj = item.get("unread");
            dto.setUnread(unreadObj != null ? ((Number) unreadObj).intValue() : 0);
            result.add(dto);
        }
        return result;
    }

    private String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}