package com.example.homemaking.config;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;

public class ChatWebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private static final ConcurrentHashMap<String, WebSocketSession> webSocketMap = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket 连接建立: sessionId={}, uri={}", session.getId(), session.getUri());
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            JsonNode json = objectMapper.readTree(payload);

            String type = json.has("type") ? json.get("type").asText() : null;

            if ("ping".equals(type)) {
                String pongJson = "{\"type\":\"pong\",\"timestamp\":" + System.currentTimeMillis() + "}";
                session.sendMessage(new TextMessage(pongJson));
                return;
            }

            if ("register".equals(type)) {
                String userId = json.has("userId") ? json.get("userId").asText() : null;
                if (userId != null && !userId.isEmpty()) {
                    webSocketMap.put(userId, session);
                    logger.info("用户注册WebSocket: userId={}", userId);
                    String ackJson = "{\"type\":\"register_ack\",\"status\":\"ok\"}";
                    session.sendMessage(new TextMessage(ackJson));
                }
                return;
            }

            String from = json.has("from") ? json.get("from").asText() : null;
            String to = json.has("to") ? json.get("to").asText() : null;
            String text = json.has("text") ? json.get("text").asText() : null;
            String roomId = json.has("roomId") ? json.get("roomId").asText() : null;

            if (from == null || from.isEmpty()) {
                sendError(session, "参数 'from' 不能为空");
                return;
            }

            webSocketMap.put(from, session);

            String responseJson = String.format(
                    "{\"from\":\"%s\",\"type\":\"%s\",\"text\":\"%s\",\"roomId\":\"%s\"}",
                    from,
                    type != null ? type : "message",
                    text != null ? text.replace("\"", "\\\"") : "",
                    roomId != null ? roomId : ""
            );

            if (to != null && !to.isEmpty()) {
                WebSocketSession targetSession = webSocketMap.get(to);
                if (targetSession != null && targetSession.isOpen()) {
                    targetSession.sendMessage(new TextMessage(responseJson));
                }
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