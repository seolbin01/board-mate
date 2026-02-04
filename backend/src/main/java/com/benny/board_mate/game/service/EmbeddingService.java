package com.benny.board_mate.game.service;

import com.benny.board_mate.game.BoardGame;
import com.benny.board_mate.game.GameEmbedding;
import com.benny.board_mate.game.GameEmbeddingRepository;
import com.benny.board_mate.game.GameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final GameRepository gameRepository;
    private final GameEmbeddingRepository embeddingRepository;
    private final ObjectMapper objectMapper;

    @Value("${sommelier.gemini.api-key:}")
    private String geminiApiKey;

    private static final String GEMINI_EMBEDDING_URL = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent";
    private static final int EMBEDDING_DIMENSION = 768;

    @Transactional
    public int generateEmbeddingsForAllGames() {
        List<BoardGame> games = gameRepository.findAll();
        log.info("임베딩 생성 시작: {} 게임", games.size());

        int created = 0;
        for (BoardGame game : games) {
            try {
                // 이미 임베딩이 존재하는지 확인
                if (embeddingRepository.existsById(game.getBggId())) {
                    continue;
                }

                String content = buildEmbeddingContent(game);
                float[] embedding = createEmbedding(content);

                if (embedding != null) {
                    GameEmbedding gameEmbedding = GameEmbedding.builder()
                            .bggId(game.getBggId())
                            .content(content)
                            .embedding(embedding)
                            .build();
                    embeddingRepository.save(gameEmbedding);
                    created++;

                    if (created % 50 == 0) {
                        log.info("임베딩 생성 진행: {}/{}", created, games.size());
                    }
                }

                // Rate limit 방지
                Thread.sleep(100);
            } catch (Exception e) {
                log.warn("임베딩 생성 실패: bggId={}, error={}", game.getBggId(), e.getMessage());
            }
        }

        log.info("임베딩 생성 완료: {} 게임", created);
        return created;
    }

    private String buildEmbeddingContent(BoardGame game) {
        StringBuilder sb = new StringBuilder();
        sb.append("게임명: ").append(game.getTitle());
        if (game.getTitleKorean() != null) {
            sb.append(" (").append(game.getTitleKorean()).append(")");
        }
        sb.append("\n");

        if (game.getDescription() != null) {
            // 설명이 너무 길면 자르기
            String desc = game.getDescription();
            if (desc.length() > 1000) {
                desc = desc.substring(0, 1000) + "...";
            }
            sb.append("설명: ").append(desc).append("\n");
        }

        sb.append("플레이어: ").append(game.getMinPlayers()).append("-").append(game.getMaxPlayers()).append("명\n");

        if (game.getPlaytime() != null) {
            sb.append("플레이 시간: ").append(game.getPlaytime()).append("분\n");
        }

        if (game.getDifficulty() != null) {
            sb.append("난이도: ").append(game.getDifficulty()).append("\n");
        }

        if (game.getMechanics() != null) {
            sb.append("메커니즘: ").append(game.getMechanics()).append("\n");
        }

        if (game.getCategories() != null) {
            sb.append("카테고리: ").append(game.getCategories()).append("\n");
        }

        return sb.toString();
    }

    public float[] createEmbedding(String text) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("Gemini API 키가 설정되지 않았습니다.");
            return null;
        }

        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(GEMINI_EMBEDDING_URL)
                    .build();

            Map<String, Object> content = Map.of(
                    "parts", List.of(Map.of("text", text))
            );
            Map<String, Object> requestBody = Map.of("content", content);

            String response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddingNode = root.path("embedding").path("values");

            List<Float> embeddingList = new ArrayList<>();
            for (JsonNode node : embeddingNode) {
                embeddingList.add(node.floatValue());
            }

            float[] embedding = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                embedding[i] = embeddingList.get(i);
            }

            return embedding;
        } catch (Exception e) {
            log.error("Gemini 임베딩 API 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    public List<Long> findSimilarGames(String query, int limit) {
        float[] queryEmbedding = createEmbedding(query);
        if (queryEmbedding == null) {
            return List.of();
        }

        String vectorString = arrayToVectorString(queryEmbedding);
        List<Object[]> results = embeddingRepository.findSimilarGames(vectorString, limit);

        return results.stream()
                .map(row -> ((Number) row[0]).longValue())
                .toList();
    }

    private String arrayToVectorString(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
