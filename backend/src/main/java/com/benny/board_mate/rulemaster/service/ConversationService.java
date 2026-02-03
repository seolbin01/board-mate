package com.benny.board_mate.rulemaster.service;

import com.benny.board_mate.rulemaster.config.RuleMasterProperties;
import com.benny.board_mate.rulemaster.dto.ConversationMessage;
import com.benny.board_mate.rulemaster.dto.ConversationSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RuleMasterProperties properties;

    private static final String KEY_PREFIX = "rulemaster:conv:";

    /**
     * 대화 세션 키 생성
     */
    private String getSessionKey(Long userId, Long bggId) {
        return KEY_PREFIX + userId + ":" + bggId;
    }

    /**
     * 대화 세션 조회
     */
    public ConversationSession getSession(Long userId, Long bggId, String gameName) {
        String key = getSessionKey(userId, bggId);
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            // 새 세션 생성
            return new ConversationSession(
                userId,
                bggId,
                gameName,
                new ArrayList<>(),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        }

        try {
            ConversationSession session = objectMapper.readValue(json, ConversationSession.class);
            // lastAccessedAt 업데이트
            session = new ConversationSession(
                session.userId(),
                session.bggId(),
                session.gameName(),
                session.messages(),
                session.createdAt(),
                LocalDateTime.now()
            );
            saveSession(session);
            return session;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse conversation session: {}", e.getMessage());
            // 파싱 실패 시 새 세션 반환
            return new ConversationSession(
                userId,
                bggId,
                gameName,
                new ArrayList<>(),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        }
    }

    /**
     * 대화 메시지 저장
     */
    public void saveMessage(Long userId, Long bggId, String gameName, String role, String content) {
        ConversationSession session = getSession(userId, bggId, gameName);

        // 새 메시지 추가
        List<ConversationMessage> messages = new ArrayList<>(session.messages());
        messages.add(new ConversationMessage(role, content, LocalDateTime.now()));

        // 최대 히스토리 제한
        int maxHistory = properties.conversation().maxHistory();
        if (messages.size() > maxHistory) {
            messages = messages.subList(messages.size() - maxHistory, messages.size());
        }

        // 세션 업데이트
        ConversationSession updatedSession = new ConversationSession(
            session.userId(),
            session.bggId(),
            session.gameName(),
            messages,
            session.createdAt(),
            LocalDateTime.now()
        );

        saveSession(updatedSession);
    }

    /**
     * 세션 저장 (내부 메서드)
     */
    private void saveSession(ConversationSession session) {
        String key = getSessionKey(session.userId(), session.bggId());
        try {
            String json = objectMapper.writeValueAsString(session);
            redisTemplate.opsForValue().set(
                key,
                json,
                properties.conversation().ttlHours(),
                TimeUnit.HOURS
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to save conversation session: {}", e.getMessage());
        }
    }

    /**
     * 대화 세션 삭제
     */
    public void clearSession(Long userId, Long bggId) {
        String key = getSessionKey(userId, bggId);
        redisTemplate.delete(key);
        log.info("Cleared conversation session: userId={}, bggId={}", userId, bggId);
    }

    /**
     * 대화 히스토리 가져오기
     */
    public List<ConversationMessage> getHistory(Long userId, Long bggId, String gameName) {
        ConversationSession session = getSession(userId, bggId, gameName);
        return new ArrayList<>(session.messages());
    }
}
