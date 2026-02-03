package com.benny.board_mate.rulemaster.client;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.rulemaster.config.RuleMasterProperties;
import com.benny.board_mate.rulemaster.dto.ConversationMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient webClient;
    private final RuleMasterProperties properties;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        당신은 '룰마스터'입니다. 보드게임 룰을 친절하고 쉽게 설명해주는 전문가예요.

        ## 현재 게임 정보
        - 게임명: %s
        - 플레이어 수: %d-%d명
        - 플레이 시간: %d분
        - 난이도: %s
        - 설명: %s

        ## 응답 스타일
        1. 한국어로 답변하세요
        2. 친근하고 대화체로 설명하세요 (마치 옆에서 같이 게임하는 친구처럼)
        3. 복잡한 룰은 단계별로 나눠서 설명하세요
        4. 예시를 들어 설명하면 좋아요
        5. 모르는 내용은 솔직히 모른다고 하고, 공식 룰북 확인을 권유하세요
        6. 이전 대화 맥락을 기억하고 자연스럽게 이어가세요

        ## 금지사항
        - 게임과 관련없는 질문에는 정중히 게임 룰 관련 질문을 해달라고 안내하세요
        - 추측이나 불확실한 정보를 확정적으로 말하지 마세요
        """;

    public GeminiClient(RuleMasterProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    public String buildSystemPrompt(String gameName, int minPlayers, int maxPlayers,
                                    Integer playingTime, String difficulty, String description) {
        String safeDescription = description != null ?
            (description.length() > 1000 ? description.substring(0, 1000) + "..." : description) : "정보 없음";

        return String.format(SYSTEM_PROMPT_TEMPLATE,
                gameName,
                minPlayers,
                maxPlayers,
                playingTime != null ? playingTime : 0,
                difficulty,
                safeDescription
        );
    }

    public Flux<String> streamChat(String systemPrompt, List<ConversationMessage> history, String userMessage) {
        try {
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
                        if (response.statusCode().value() == 429) {
                            return response.createException()
                                    .map(e -> new BusinessException(ErrorCode.RULEMASTER_RATE_LIMIT));
                        }
                        return response.createException()
                                .map(e -> new BusinessException(ErrorCode.RULEMASTER_GEMINI_ERROR));
                    })
                    .onStatus(status -> status.is5xxServerError(), response -> {
                        log.error("Gemini API 5xx 에러: {}", response.statusCode());
                        return response.createException()
                                .map(e -> new BusinessException(ErrorCode.RULEMASTER_GEMINI_ERROR));
                    })
                    .bodyToFlux(String.class)
                    .filter(line -> line.startsWith("data: "))
                    .map(line -> line.substring(6))
                    .filter(data -> !data.isEmpty() && !data.equals("[DONE]"))
                    .mapNotNull(this::extractTextFromGeminiResponse)
                    .onErrorMap(e -> {
                        if (e instanceof BusinessException) return e;
                        log.error("Gemini API 호출 실패", e);
                        return new BusinessException(ErrorCode.RULEMASTER_GEMINI_ERROR);
                    });

        } catch (Exception e) {
            log.error("Gemini 요청 생성 실패", e);
            return Flux.error(new BusinessException(ErrorCode.RULEMASTER_GEMINI_ERROR));
        }
    }

    private Map<String, Object> buildGeminiRequest(String systemPrompt, List<ConversationMessage> history, String userMessage) {
        Map<String, Object> request = new HashMap<>();

        // System instruction
        Map<String, Object> systemInstruction = new HashMap<>();
        Map<String, String> systemPart = new HashMap<>();
        systemPart.put("text", systemPrompt);
        systemInstruction.put("parts", List.of(systemPart));
        request.put("systemInstruction", systemInstruction);

        // Contents (conversation history + current message)
        List<Map<String, Object>> contents = new ArrayList<>();

        for (ConversationMessage msg : history) {
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user".equals(msg.role()) ? "user" : "model");
            Map<String, String> part = new HashMap<>();
            part.put("text", msg.content());
            content.put("parts", List.of(part));
            contents.add(content);
        }

        // Add current user message
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
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText(null);
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("Gemini 응답 파싱 실패: {}", jsonData);
            return null;
        }
    }
}
