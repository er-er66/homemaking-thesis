package com.example.homemaking.controller.chatController;

import com.example.homemaking.config.ChatWebSocketHandler;
import com.example.homemaking.dto.ChatMessageDTO;
import com.example.homemaking.dto.ChatRoomDTO;
import com.example.homemaking.dto.UnreadMessageDTO;
import com.example.homemaking.entity.ChatMessage;
import com.example.homemaking.result.Result;
import com.example.homemaking.services.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/chat")
public class chatController {
    @Autowired
    private ChatService chatService;

    @GetMapping("/onlineCount")
    public Result<Integer> getOnlineCount() {
        return Result.success(ChatWebSocketHandler.getOnlineCount());
    }

    @GetMapping("/health")
    public Result<String> healthCheck() {
        return Result.success("WebSocket服务运行正常");
    }

    @GetMapping("/history")
    public Result<?> getChatHistory(@RequestParam("room_id") String roomId) {
        log.info("获取聊天记录，roomId：{}", roomId);
        List<ChatMessage> history = chatService.getHistory(roomId);
        return Result.success(history);
    }

    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody ChatMessageDTO messageDTO) {
        int count = chatService.sendMessage(messageDTO);
        if (count == 0) {
            return Result.error("消息发送失败");
        }
        return Result.success("消息发送成功");
    }

    @GetMapping("/unread")
    public Result<?> getUnreadMessages(@RequestParam("userId") String userId) {
        List<UnreadMessageDTO> unreadList = chatService.getUnreadMessages(userId);
        return Result.success(unreadList);
    }

    @GetMapping("/merchants")
    public Result<?> getMerchantChatHistory(@RequestParam("userId") String userId) {
        List<String> staffList = chatService.getSysStaffList(userId);
        log.info("获取用户聊天的商户列表，id：{}", staffList);
        return Result.success(staffList);
    }

    @PostMapping("/read")
    public Result<String> markRead(@RequestParam("room_id") String roomId,
                                   @RequestParam("userId") String userId) {
        log.info("标记已读，roomId：{}", roomId);
        chatService.markRead(roomId, userId);
        return Result.success("标记已读成功");
    }

    @GetMapping("/rooms")
    public Result<?> getChatRooms(@RequestParam("userId") String userId) {
        List<ChatRoomDTO> rooms = chatService.getChatRooms(userId);
        return Result.success(rooms);
    }
}