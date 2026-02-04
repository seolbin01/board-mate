package com.benny.board_mate.sommelier.controller;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.sommelier.dto.ChatMessage;
import com.benny.board_mate.sommelier.dto.SommelierRequest;
import com.benny.board_mate.sommelier.dto.SommelierResponse;
import com.benny.board_mate.sommelier.service.SommelierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Tag(name = "Sommelier", description = "보드게임 소믈리에 - AI 기반 게임 추천 챗봇")
@RestController
@RequestMapping("/api/sommelier")
@RequiredArgsConstructor
@Slf4j
public class SommelierController {

    private final SommelierService sommelierService;

    @Operation(summary = "AI 채팅", description = "보드게임 추천을 위한 AI 챗봇과 대화 (SSE 스트리밍)")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SommelierResponse>> chat(
            @Valid @RequestBody SommelierRequest request
    ) {
        log.info("소믈리에 채팅 요청: sessionId={}", request.sessionId());

        return sommelierService.chat(request.sessionId(), request.message())
                .map(response -> ServerSentEvent.<SommelierResponse>builder()
                        .data(response)
                        .build())
                .concatWith(Flux.just(ServerSentEvent.<SommelierResponse>builder()
                        .comment("Stream completed")
                        .build()))
                .onErrorResume(error -> {
                    log.error("SSE 스트리밍 오류", error);
                    return Flux.just(ServerSentEvent.<SommelierResponse>builder()
                            .data(SommelierResponse.error("스트리밍 중 오류가 발생했습니다."))
                            .build());
                })
                .delayElements(Duration.ofMillis(10));
    }

    @Operation(summary = "대화 기록 조회", description = "세션의 대화 기록을 조회합니다")
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getHistory(
            @Parameter(description = "세션 ID") @PathVariable String sessionId
    ) {
        log.info("소믈리에 히스토리 조회: sessionId={}", sessionId);
        List<ChatMessage> history = sommelierService.getHistory(sessionId);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }

    @Operation(summary = "대화 기록 삭제", description = "세션의 대화 기록을 초기화합니다")
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> clearHistory(
            @Parameter(description = "세션 ID") @PathVariable String sessionId
    ) {
        log.info("소믈리에 히스토리 삭제: sessionId={}", sessionId);
        sommelierService.clearHistory(sessionId);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
