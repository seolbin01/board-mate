package com.benny.board_mate.sommelier.service;

import com.benny.board_mate.sommelier.dto.ChatMessage;
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
        try {
            String key = getKey(sessionId);
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<ChatMessage>>() {});
        } catch (Exception e) {
            log.warn("Redis 히스토리 조회 실패 (무시): {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public void addMessage(String sessionId, ChatMessage message) {
        try {
            List<ChatMessage> history = getHistory(sessionId);
            history.add(message);
            if (history.size() > MAX_HISTORY) {
                history = history.subList(history.size() - MAX_HISTORY, history.size());
            }
            saveHistory(sessionId, history);
        } catch (Exception e) {
            log.warn("Redis 메시지 추가 실패 (무시): {}", e.getMessage());
        }
    }

    public void saveHistory(String sessionId, List<ChatMessage> history) {
        try {
            String key = getKey(sessionId);
            String json = objectMapper.writeValueAsString(history);
            redisTemplate.opsForValue().set(key, json, TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis 히스토리 저장 실패 (무시): {}", e.getMessage());
        }
    }

    public void clearHistory(String sessionId) {
        try {
            String key = getKey(sessionId);
            redisTemplate.delete(key);
            log.info("Cleared sommelier history: sessionId={}", sessionId);
        } catch (Exception e) {
            log.warn("Redis 히스토리 삭제 실패 (무시): {}", e.getMessage());
        }
    }
}
