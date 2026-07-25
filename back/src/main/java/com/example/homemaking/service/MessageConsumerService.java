package com.example.homemaking.service;

import com.example.homemaking.entity.ChatMessage;
import com.example.homemaking.mapper.ChatMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MessageConsumerService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChatMapper chatMapper;

    private static final String CHAT_QUEUE_KEY = "chat:msg:queue";
    private static final int BATCH_SIZE = 50;

    private final ObjectMapper objectMapper;

    public MessageConsumerService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Scheduled(fixedDelay = 100)
    @Transactional
    public void consumeMessages() {
        List<ChatMessage> batch = new ArrayList<>();

        try {
            for (int i = 0; i < BATCH_SIZE; i++) {
                Object obj = redisTemplate.opsForList().rightPop(CHAT_QUEUE_KEY);
                if (obj == null) {
                    break;
                }

                ChatMessage message;
                if (obj instanceof ChatMessage) {
                    message = (ChatMessage) obj;
                } else if (obj instanceof String) {
                    message = objectMapper.readValue((String) obj, ChatMessage.class);
                } else if (obj instanceof java.util.LinkedHashMap) {
                    String json = objectMapper.writeValueAsString(obj);
                    message = objectMapper.readValue(json, ChatMessage.class);
                } else {
                    log.warn("Unknown message type: {}", obj.getClass());
                    continue;
                }
                batch.add(message);
            }

            if (!batch.isEmpty()) {
                int count = chatMapper.batchInsert(batch);
                log.info("批量插入 {} 条消息", count);
            }

        } catch (JsonProcessingException e) {
            log.error("消息反序列化失败", e);
        } catch (Exception e) {
            log.error("消息消费异常", e);
            if (!batch.isEmpty()) {
                for (int i = batch.size() - 1; i >= 0; i--) {
                    redisTemplate.opsForList().leftPush(CHAT_QUEUE_KEY, batch.get(i));
                }
            }
        }
    }
}