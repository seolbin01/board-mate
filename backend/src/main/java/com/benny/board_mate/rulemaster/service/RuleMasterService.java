package com.benny.board_mate.rulemaster.service;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.rulemaster.client.BggApiClient;
import com.benny.board_mate.rulemaster.client.GeminiClient;
import com.benny.board_mate.rulemaster.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleMasterService {

    private final BggApiClient bggApiClient;
    private final GeminiClient geminiClient;
    private final ConversationService conversationService;

    /**
     * 게임 검색
     */
    public List<GameSearchResponse> searchGames(String query, int limit) {
        log.info("게임 검색 요청: query={}, limit={}", query, limit);

        List<GameSearchResponse> results = bggApiClient.searchGames(query);

        // limit 적용
        if (results.size() > limit) {
            results = results.subList(0, limit);
        }

        return results;
    }

    /**
     * 게임 상세 조회
     */
    public GameDetailResponse getGameDetail(Long bggId) {
        log.info("게임 상세 조회 요청: bggId={}", bggId);
        return bggApiClient.getGameDetail(bggId);
    }

    /**
     * 채팅 (SSE 스트리밍)
     */
    public Flux<ChatResponse> chat(Long userId, ChatRequest request) {
        log.info("채팅 요청: userId={}, bggId={}", userId, request.bggId());

        try {
            // 1. 게임 정보 조회
            GameDetailResponse gameDetail = bggApiClient.getGameDetail(request.bggId());

            // 2. 시스템 프롬프트 생성
            String systemPrompt = geminiClient.buildSystemPrompt(
                gameDetail.name(),
                gameDetail.minPlayers() != null ? gameDetail.minPlayers() : 1,
                gameDetail.maxPlayers() != null ? gameDetail.maxPlayers() : 10,
                gameDetail.playingTime(),
                formatDifficulty(gameDetail.weight()),
                gameDetail.description()
            );

            // 3. 대화 히스토리 로드
            List<ConversationMessage> history = conversationService.getHistory(
                userId,
                request.bggId(),
                gameDetail.name()
            );

            // 4. 사용자 메시지 저장
            conversationService.saveMessage(
                userId,
                request.bggId(),
                gameDetail.name(),
                "user",
                request.message()
            );

            // 5. Gemini API 호출 (스트리밍)
            String messageId = UUID.randomUUID().toString();
            AtomicReference<StringBuilder> fullResponse = new AtomicReference<>(new StringBuilder());

            return geminiClient.streamChat(systemPrompt, history, request.message())
                .map(content -> {
                    fullResponse.get().append(content);
                    return ChatResponse.content(content);
                })
                .concatWith(Flux.defer(() -> {
                    // 6. 어시스턴트 응답 저장
                    conversationService.saveMessage(
                        userId,
                        request.bggId(),
                        gameDetail.name(),
                        "assistant",
                        fullResponse.get().toString()
                    );
                    return Flux.just(ChatResponse.done(messageId));
                }))
                .onErrorResume(error -> {
                    log.error("채팅 스트리밍 오류: userId={}, bggId={}", userId, request.bggId(), error);

                    if (error instanceof BusinessException be) {
                        ErrorCode errorCode = be.getErrorCode();
                        return Flux.just(ChatResponse.error(
                            errorCode.name(),
                            errorCode.getMessage(),
                            errorCode == ErrorCode.RULEMASTER_RATE_LIMIT
                        ));
                    }

                    return Flux.just(ChatResponse.error(
                        "INTERNAL_ERROR",
                        "채팅 처리 중 오류가 발생했습니다.",
                        true
                    ));
                });

        } catch (BusinessException e) {
            log.error("채팅 초기화 실패: userId={}, bggId={}", userId, request.bggId(), e);
            return Flux.just(ChatResponse.error(
                e.getErrorCode().name(),
                e.getErrorCode().getMessage(),
                false
            ));
        } catch (Exception e) {
            log.error("채팅 처리 중 예상치 못한 오류: userId={}, bggId={}", userId, request.bggId(), e);
            return Flux.just(ChatResponse.error(
                "INTERNAL_ERROR",
                "채팅 처리 중 오류가 발생했습니다.",
                true
            ));
        }
    }

    /**
     * 대화 히스토리 조회
     */
    public ConversationSession getConversation(Long userId, Long bggId) {
        log.info("대화 히스토리 조회: userId={}, bggId={}", userId, bggId);

        // 게임 정보 조회하여 게임명 가져오기
        GameDetailResponse gameDetail = bggApiClient.getGameDetail(bggId);

        return conversationService.getSession(userId, bggId, gameDetail.name());
    }

    /**
     * 대화 초기화
     */
    public void clearConversation(Long userId, Long bggId) {
        log.info("대화 초기화 요청: userId={}, bggId={}", userId, bggId);
        conversationService.clearSession(userId, bggId);
    }

    /**
     * 난이도 포맷팅 (weight 값을 한글로)
     */
    private String formatDifficulty(Double weight) {
        if (weight == null) {
            return "알 수 없음";
        }

        if (weight < 2.0) {
            return "쉬움 (초보자용)";
        } else if (weight < 3.0) {
            return "보통 (가족용)";
        } else if (weight < 4.0) {
            return "어려움 (게이머용)";
        } else {
            return "매우 어려움 (전문가용)";
        }
    }
}
