package com.benny.board_mate.rulemaster.controller;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.rulemaster.dto.*;
import com.benny.board_mate.rulemaster.service.RuleMasterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/rulemaster")
@RequiredArgsConstructor
@Slf4j
public class RuleMasterController {

    private final RuleMasterService ruleMasterService;

    /**
     * 게임 검색
     * GET /api/rulemaster/games/search?query=카탄&limit=10
     */
    @GetMapping("/games/search")
    public ResponseEntity<ApiResponse<List<GameSearchResponse>>> searchGames(
        @RequestParam String query,
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("게임 검색 API 호출: query={}, limit={}", query, limit);
        List<GameSearchResponse> results = ruleMasterService.searchGames(query, limit);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    /**
     * 게임 상세 조회
     * GET /api/rulemaster/games/{bggId}
     */
    @GetMapping("/games/{bggId}")
    public ResponseEntity<ApiResponse<GameDetailResponse>> getGameDetail(
        @PathVariable Long bggId
    ) {
        log.info("게임 상세 조회 API 호출: bggId={}", bggId);
        GameDetailResponse detail = ruleMasterService.getGameDetail(bggId);
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    /**
     * 채팅 (SSE 스트리밍)
     * POST /api/rulemaster/chat
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatResponse>> chat(
        Authentication authentication,
        @Valid @RequestBody ChatRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("채팅 API 호출: userId={}, bggId={}", userId, request.bggId());

        return ruleMasterService.chat(userId, request)
            .map(response -> ServerSentEvent.<ChatResponse>builder()
                .data(response)
                .build())
            .concatWith(Flux.just(ServerSentEvent.<ChatResponse>builder()
                .comment("Stream completed")
                .build()))
            .onErrorResume(error -> {
                log.error("SSE 스트리밍 오류", error);
                return Flux.just(ServerSentEvent.<ChatResponse>builder()
                    .data(ChatResponse.error(
                        "STREAM_ERROR",
                        "스트리밍 중 오류가 발생했습니다.",
                        true
                    ))
                    .build());
            })
            .delayElements(Duration.ofMillis(10)); // 백프레셔 방지
    }

    /**
     * 대화 히스토리 조회
     * GET /api/rulemaster/conversations?bggId=123
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<ConversationSession>> getConversation(
        Authentication authentication,
        @RequestParam Long bggId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("대화 히스토리 조회 API 호출: userId={}, bggId={}", userId, bggId);

        ConversationSession session = ruleMasterService.getConversation(userId, bggId);
        return ResponseEntity.ok(ApiResponse.ok(session));
    }

    /**
     * 대화 초기화
     * DELETE /api/rulemaster/conversations?bggId=123
     */
    @DeleteMapping("/conversations")
    public ResponseEntity<ApiResponse<Void>> clearConversation(
        Authentication authentication,
        @RequestParam Long bggId
    ) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("대화 초기화 API 호출: userId={}, bggId={}", userId, bggId);

        ruleMasterService.clearConversation(userId, bggId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
