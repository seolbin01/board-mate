package com.benny.board_mate.sommelier.service;

import com.benny.board_mate.game.BoardGame;
import com.benny.board_mate.game.GameRepository;
import com.benny.board_mate.game.service.EmbeddingService;
import com.benny.board_mate.sommelier.config.SommelierProperties;
import com.benny.board_mate.sommelier.dto.ChatMessage;
import com.benny.board_mate.sommelier.dto.ConversationMessage;
import com.benny.board_mate.sommelier.dto.SommelierResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import java.time.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SommelierService {

    private final EmbeddingService embeddingService;
    private final GameRepository gameRepository;
    private final SommelierHistoryService historyService;
    private final SommelierProperties properties;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    private static final String SYSTEM_PROMPT = """
        당신은 '보드게임 소믈리에'입니다. 사용자의 취향과 상황에 맞는 보드게임을 추천해주는 전문가예요.

        ## 응답 스타일
        1. 한국어로 답변하세요
        2. 친근하고 전문적인 소믈리에처럼 대화하세요
        3. 게임 이름은 **굵게** 표시하세요
        4. 추천할 때는 왜 그 게임을 추천하는지 이유를 설명하세요
        5. 비교 요청 시 장단점을 명확히 비교하세요
        6. 모르는 게임은 솔직히 "죄송합니다, 해당 게임 정보가 없습니다"라고 답변하세요

        ## 참고할 게임 정보
        %s

        이 정보를 바탕으로 사용자의 질문에 답변해주세요.
        """;

    public SommelierService(
            EmbeddingService embeddingService,
            GameRepository gameRepository,
            SommelierHistoryService historyService,
            SommelierProperties properties,
            ObjectMapper objectMapper
    ) {
        this.embeddingService = embeddingService;
        this.gameRepository = gameRepository;
        this.historyService = historyService;
        this.properties = properties;
        this.objectMapper = objectMapper;
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60));
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public Flux<SommelierResponse> chat(String sessionId, String userMessage) {
        log.info("소믈리에 채팅 요청: sessionId={}, message={}", sessionId, userMessage);

        // 1. 대화 히스토리 먼저 가져오기 (현재 메시지 저장 전)
        List<ChatMessage> history = historyService.getHistory(sessionId);
        List<ConversationMessage> conversationHistory = history.stream()
                .map(msg -> new ConversationMessage(msg.role(), msg.content(), msg.timestamp()))
                .collect(Collectors.toList());

        // 2. RAG: 관련 게임 검색
        List<Long> similarGameIds = embeddingService.findSimilarGames(userMessage, 5);
        List<BoardGame> relevantGames = gameRepository.findAllById(similarGameIds);
        String gameContext = buildGameContext(relevantGames);

        // 3. 시스템 프롬프트 구성
        String systemPrompt = String.format(SYSTEM_PROMPT, gameContext);

        // 4. Gemini 스트리밍 호출
        StringBuilder fullResponse = new StringBuilder();

        return streamGemini(systemPrompt, conversationHistory, userMessage)
                .map(text -> {
                    fullResponse.append(text);
                    return SommelierResponse.text(text);
                })
                .doOnComplete(() -> {
                    // 응답 완료 시 사용자 메시지와 응답 모두 저장
                    historyService.addMessage(sessionId, ChatMessage.user(userMessage));
                    historyService.addMessage(sessionId, ChatMessage.assistant(fullResponse.toString()));
                })
                .concatWith(Flux.just(SommelierResponse.done()))
                .doOnError(error -> log.error("스트림 오류 발생: {}", error.getMessage(), error))
                .onErrorResume(error -> {
                    log.error("소믈리에 채팅 오류: {}", error.getClass().getSimpleName(), error);
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "알 수 없는 오류";
                    return Flux.just(SommelierResponse.error("죄송합니다, 오류가 발생했습니다: " + errorMsg));
                });
    }

    private String buildGameContext(List<BoardGame> games) {
        if (games.isEmpty()) {
            return "관련 게임 정보가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        for (BoardGame game : games) {
            sb.append("- **").append(game.getTitle()).append("**");
            if (game.getTitleKorean() != null) {
                sb.append(" (").append(game.getTitleKorean()).append(")");
            }
            sb.append("\n");
            sb.append("  - 플레이어: ").append(game.getMinPlayers()).append("-").append(game.getMaxPlayers()).append("명\n");
            if (game.getPlaytime() != null) {
                sb.append("  - 플레이 시간: ").append(game.getPlaytime()).append("분\n");
            }
            if (game.getDifficulty() != null) {
                sb.append("  - 난이도: ").append(game.getDifficulty()).append("\n");
            }
            if (game.getMechanics() != null) {
                sb.append("  - 메커니즘: ").append(game.getMechanics()).append("\n");
            }
            if (game.getDescription() != null) {
                String desc = game.getDescription();
                if (desc.length() > 200) {
                    desc = desc.substring(0, 200) + "...";
                }
                sb.append("  - 설명: ").append(desc).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private Flux<String> streamGemini(String systemPrompt, List<ConversationMessage> history, String userMessage) {
        Map<String, Object> requestBody = buildGeminiRequest(systemPrompt, history, userMessage);
        String apiKey = properties.gemini().apiKey();


        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/models/{model}:streamGenerateContent")
                        .queryParam("key", apiKey)
                        .queryParam("alt", "sse")
                        .build(properties.gemini().model()))
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response -> {
                    log.error("Gemini API 4xx 에러: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .doOnNext(body -> log.error("에러 응답: {}", body))
                            .then(response.createException());
                })
                .onStatus(status -> status.is5xxServerError(), response -> {
                    log.error("Gemini API 5xx 에러: {}", response.statusCode());
                    return response.createException();
                })
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .map(ServerSentEvent::data)
                .filter(data -> data != null && !data.isEmpty())
                .mapNotNull(this::extractTextFromGeminiResponse);
    }

    private Map<String, Object> buildGeminiRequest(String systemPrompt, List<ConversationMessage> history, String userMessage) {
        Map<String, Object> request = new HashMap<>();

        // System instruction
        Map<String, Object> systemInstruction = new HashMap<>();
        Map<String, String> systemPart = new HashMap<>();
        systemPart.put("text", systemPrompt);
        systemInstruction.put("parts", List.of(systemPart));
        request.put("systemInstruction", systemInstruction);

        // Contents
        List<Map<String, Object>> contents = new ArrayList<>();

        for (ConversationMessage msg : history) {
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user".equals(msg.role()) ? "user" : "model");
            Map<String, String> part = new HashMap<>();
            part.put("text", msg.content());
            content.put("parts", List.of(part));
            contents.add(content);
        }

        // Current user message
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        Map<String, String> userPart = new HashMap<>();
        userPart.put("text", userMessage);
        userContent.put("parts", List.of(userPart));
        contents.add(userContent);

        request.put("contents", contents);

        // Generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", properties.gemini().temperature());
        generationConfig.put("maxOutputTokens", properties.gemini().maxTokens());
        request.put("generationConfig", generationConfig);

        return request;
    }

    private String extractTextFromGeminiResponse(String jsonData) {
        try {
            JsonNode root = objectMapper.readTree(jsonData);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText(null);
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("Gemini 응답 파싱 실패: {}", jsonData);
            return null;
        }
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return historyService.getHistory(sessionId);
    }

    public void clearHistory(String sessionId) {
        historyService.clearHistory(sessionId);
    }
}
