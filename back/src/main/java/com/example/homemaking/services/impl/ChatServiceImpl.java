package com.example.homemaking.services.impl;

import com.example.homemaking.dto.ChatMessageDTO;
import com.example.homemaking.dto.UnreadMessageDTO;
import com.example.homemaking.entity.ChatMessage;
import com.example.homemaking.entity.SysUser;
import com.example.homemaking.mapper.ChatMapper;
import com.example.homemaking.services.ChatService;
import com.example.homemaking.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMapper chatMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CHAT_QUEUE_KEY = "chat:msg:queue";


    @Override
    public List<ChatMessage> getHistory(String senderId, String receiverId) {
        List<ChatMessage> chatMessages = chatMapper.getHistory(senderId, receiverId);

        return chatMessages;
    }

    @Override
    public int sendMessage(ChatMessageDTO messageDTO) {
        ChatMessage message = new ChatMessage();
        String roomId = messageDTO.getSenderId() + "_" + messageDTO.getReceiverId();
        BeanUtils.copyProperties(messageDTO, message);
        message.setIsRead("0");
        message.setRoomId(roomId);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        
        redisTemplate.opsForList().leftPush(CHAT_QUEUE_KEY, message);
        log.info("消息已写入Redis队列: roomId={}, senderId={}", roomId, messageDTO.getSenderId());
        
        return 1;
    }

    /**
     * 获取未读消息
     *
     * @param userId
     * @return
     */
    @Override
    public List<UnreadMessageDTO> getUnreadMessages(String userId) {
        List<Map<String, Object>> rawData = chatMapper.getUnreadMessagesByReceiver(userId);
        List<UnreadMessageDTO> result = new ArrayList<>();

        for (Map<String, Object> item : rawData) {
            String merchantId = (String) item.get("merchantId");
            SysUser merchant = userService.getUserByAccount(merchantId);

            UnreadMessageDTO dto = new UnreadMessageDTO();
            dto.setMerchantId(merchantId);
            dto.setMerchantName(merchant != null ? merchant.getUsername() : "未知商家");
            dto.setMerchantAvatar(merchant != null ? merchant.getAvatar() : "");
            dto.setLastMsg((String) item.get("lastMsg"));

            // 格式化时间
            Object timeObj = item.get("time");
            if (timeObj instanceof Date) {
                dto.setTime(formatDateTime((Date) timeObj));
            } else {
                dto.setTime(timeObj != null ? timeObj.toString() : "");
            }

            dto.setUnreadCount(((Number) item.get("unreadCount")).intValue());
            result.add(dto);
        }

        return result;
    }

    private String formatDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    @Override
    public List<String> getSysStaffList(String userId) {
        List<String> sysStaffListAll = chatMapper.getSysStaffList(userId);
        List<String>  sysStaffList = new ArrayList<>();
        if (sysStaffListAll.size() > 0) {
            for (int i = 0; i < sysStaffListAll.size(); i++) {
                if (sysStaffListAll.get(i).equals(userId)) {
                    sysStaffListAll.remove(i);
                }
            }
       sysStaffList = new ArrayList<>(new HashSet<>(sysStaffListAll));

        }else {
            sysStaffList.add("无聊天记录");
        }

      return sysStaffList; //返回的就是商家id
    }
}