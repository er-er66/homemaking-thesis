package com.example.homemaking.config;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;  // 使用 Jackson
import com.fasterxml.jackson.databind.JsonNode;      // 使用 Jackson
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private static final ConcurrentHashMap<String, WebSocketSession> webSocketMap = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            JsonNode json = objectMapper.readTree(payload);

            String type = json.has("type") ? json.get("type").asText() : null;

            // 处理心跳消息
            if ("ping".equals(type)) {
                String pongJson = "{\"type\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}";
                session.sendMessage(new TextMessage(pongJson));
                return;
            }

            // 处理普通消息
            String from = json.has("from") ? json.get("from").asText() : null;
            String to = json.has("to") ? json.get("to").asText() : null;
            String text = json.has("text") ? json.get("text").asText() : null;

            if (from == null || from.isEmpty()) {
                sendError(session, "参数 'from' 不能为空");
                return;
            }
            if (to == null || to.isEmpty()) {
                sendError(session, "参数 'to' 不能为空");
                return;
            }

            webSocketMap.put(from, session);

            // 构建响应
            String responseJson = String.format(
                    "{\"from\":\"%s\",\"type\":\"%s\",\"text\":\"%s\"}",
                    from, type, text != null ? text.replace("\"", "\\\"") : ""
            );

            WebSocketSession targetSession = webSocketMap.get(to);
            if (targetSession != null && targetSession.isOpen()) {
                targetSession.sendMessage(new TextMessage(responseJson));
            }

        } catch (Exception e) {
            logger.error("消息处理失败: {}", e.getMessage(), e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            String errorJson = "{\"type\":\"error\",\"message\":\"" + errorMessage.replace("\"", "\\\"") + "\"}";
            session.sendMessage(new TextMessage(errorJson));
        } catch (Exception e) {
            logger.error("发送错误消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketMap.entrySet().removeIf(entry -> entry.getValue().equals(session));
        super.afterConnectionClosed(session, status);
    }
    public static int getOnlineCount() {
        return webSocketMap.size();
    }
}