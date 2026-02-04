package com.benny.board_mate.sommelier.service;

import com.benny.board_mate.sommelier.dto.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SommelierHistoryService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public SommelierHistoryService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        // 타입 정보 없이 깔끔한 JSON 직렬화를 위해 새 ObjectMapper 생성
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    private static final String KEY_PREFIX = "sommelier:history:";
    private static final int MAX_HISTORY = 20;
    private static final int TTL_HOURS = 24;

    private String getKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    public List<ChatMessage> getHistory(String sessionId) {
        String key = getKey(sessionId);
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse chat history: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void addMessage(String sessionId, ChatMessage message) {
        List<ChatMessage> history = getHistory(sessionId);
        history.add(message);

        if (history.size() > MAX_HISTORY) {
            history = history.subList(history.size() - MAX_HISTORY, history.size());
        }

        saveHistory(sessionId, history);
    }

    public void saveHistory(String sessionId, List<ChatMessage> history) {
        String key = getKey(sessionId);
        try {
            String json = objectMapper.writeValueAsString(history);
            redisTemplate.opsForValue().set(key, json, TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Failed to save chat history: {}", e.getMessage());
        }
    }

    public void clearHistory(String sessionId) {
        String key = getKey(sessionId);
        redisTemplate.delete(key);
        log.info("Cleared sommelier history: sessionId={}", sessionId);
    }
}
