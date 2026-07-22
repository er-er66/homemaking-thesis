package com.example.homemaking.controller.chatController;

import com.example.homemaking.config.ChatWebSocketHandler;
import com.example.homemaking.dto.ChatMessageDTO;
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
@RequestMapping("/api/chat")
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
    /**
     * 获取聊天记录
     * GET /api/chat/history?sender_id=xxx&receiver_id=xxx
     */
    @GetMapping("/history")
    public Result<?> getChatHistory(
            @RequestParam("sender_id") String senderId,
            @RequestParam("receiver_id") String receiverId
    ) {
        // 调用业务逻辑获取聊天记录
        log.info("获取聊天记录，发送者id：{}，接收者id：{}", senderId, receiverId);
        List<ChatMessage> history = chatService.getHistory(senderId, receiverId);
        return Result.success(history);
    }

    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody ChatMessageDTO messageDTO) {
       int count = chatService.sendMessage(messageDTO);
       if(count == 0){
           return Result.error("消息发送失败");
       }
        return Result.success("消息发送成功");
    }
    /**
     * 获取用户的未读消息列表
     * GET /api/chat/unread?userId=xxx
     */
    @GetMapping("/unread")
    public Result<?> getUnreadMessages(@RequestParam("userId") String userId) {
        List<UnreadMessageDTO> unreadList = chatService.getUnreadMessages(userId);
        return Result.success(unreadList);
    }

    /**
     * 获取用户聊天的商户列表
     * GET /api/chat/merchants?userId=xxx
     */
    @GetMapping("/merchants")
    public Result<?> getMerchantChatHistory(@RequestParam("userId") String userId) {
        List<String> staffList = chatService.getSysStaffList(userId);
        log.info("获取用户聊天的商户列表，id：{}", staffList);
        return Result.success(staffList);
    }

}
