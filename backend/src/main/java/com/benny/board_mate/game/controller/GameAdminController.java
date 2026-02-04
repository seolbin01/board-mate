package com.benny.board_mate.game.controller;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.game.service.EmbeddingService;
import com.benny.board_mate.game.service.GameDataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/games")
@RequiredArgsConstructor
public class GameAdminController {

    private final GameDataImportService importService;
    private final EmbeddingService embeddingService;

    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importGames(
            @RequestParam String filePath,
            @RequestParam(defaultValue = "1000") int limit
    ) {
        log.info("게임 데이터 임포트 요청: filePath={}, limit={}", filePath, limit);

        try {
            int imported = importService.importFromCsv(filePath, limit);
            return ApiResponse.ok(Map.of(
                    "imported", imported,
                    "message", imported + "개 게임이 임포트되었습니다."
            ));
        } catch (Exception e) {
            log.error("게임 데이터 임포트 실패", e);
            throw new RuntimeException("임포트 실패: " + e.getMessage());
        }
    }

    @PostMapping("/embeddings")
    public ApiResponse<Map<String, Object>> generateEmbeddings() {
        log.info("임베딩 생성 요청");

        try {
            int created = embeddingService.generateEmbeddingsForAllGames();
            return ApiResponse.ok(Map.of(
                    "created", created,
                    "message", created + "개 게임의 임베딩이 생성되었습니다."
            ));
        } catch (Exception e) {
            log.error("임베딩 생성 실패", e);
            throw new RuntimeException("임베딩 생성 실패: " + e.getMessage());
        }
    }
}
