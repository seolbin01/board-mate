package com.benny.board_mate.game.service;

import com.benny.board_mate.game.BoardGame;
import com.benny.board_mate.game.GameRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameDataImportService {

    private final GameRepository gameRepository;

    @Transactional
    public int importFromCsv(String filePath, int limit) throws IOException, CsvException {
        log.info("CSV 임포트 시작: filePath={}, limit={}", filePath, limit);

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> allRows = reader.readAll();

            if (allRows.isEmpty()) {
                log.warn("CSV 파일이 비어 있습니다.");
                return 0;
            }

            // 헤더 파싱
            String[] headers = allRows.get(0);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(headers[i].trim(), i);
            }

            log.info("CSV 헤더: {}", Arrays.toString(headers));

            // 데이터 행들 (헤더 제외)
            List<String[]> dataRows = allRows.subList(1, allRows.size());

            // NumUserRatings 기준 정렬하여 상위 limit개 선택
            int numRatingsIdx = headerIndex.getOrDefault("NumUserRatings", -1);
            if (numRatingsIdx >= 0) {
                dataRows.sort((a, b) -> {
                    int ratingA = parseIntSafe(a.length > numRatingsIdx ? a[numRatingsIdx] : "0");
                    int ratingB = parseIntSafe(b.length > numRatingsIdx ? b[numRatingsIdx] : "0");
                    return Integer.compare(ratingB, ratingA);
                });
            }

            List<String[]> topGames = dataRows.subList(0, Math.min(limit, dataRows.size()));
            log.info("상위 {} 게임 선택 완료", topGames.size());

            // 배치 저장
            List<BoardGame> batch = new ArrayList<>();
            int imported = 0;

            for (String[] row : topGames) {
                try {
                    BoardGame game = mapToEntity(row, headerIndex);
                    if (game != null) {
                        batch.add(game);
                    }

                    if (batch.size() >= 100) {
                        gameRepository.saveAll(batch);
                        imported += batch.size();
                        log.info("임포트 진행: {} 게임 저장됨", imported);
                        batch.clear();
                    }
                } catch (Exception e) {
                    log.warn("게임 파싱 실패: {}", e.getMessage());
                }
            }

            // 나머지 저장
            if (!batch.isEmpty()) {
                gameRepository.saveAll(batch);
                imported += batch.size();
            }

            log.info("CSV 임포트 완료: 총 {} 게임 저장됨", imported);
            return imported;
        }
    }

    private BoardGame mapToEntity(String[] row, Map<String, Integer> headerIndex) {
        Long bggId = parseLongSafe(getValue(row, headerIndex, "BGGId"));
        if (bggId == null) {
            return null;
        }

        // 이미 존재하는지 확인
        if (gameRepository.findByBggId(bggId).isPresent()) {
            return null;
        }

        String title = getValue(row, headerIndex, "Name");
        if (title == null || title.isBlank()) {
            return null;
        }

        return BoardGame.builder()
                .bggId(bggId)
                .title(title)
                .minPlayers(parseIntWithDefault(getValue(row, headerIndex, "MinPlayers"), 1))
                .maxPlayers(parseIntWithDefault(getValue(row, headerIndex, "MaxPlayers"), 4))
                .playtime(parseIntSafe(getValue(row, headerIndex, "MfgPlaytime")))
                .minPlaytime(parseIntSafe(getValue(row, headerIndex, "ComMinPlaytime")))
                .maxPlaytime(parseIntSafe(getValue(row, headerIndex, "ComMaxPlaytime")))
                .weight(parseDoubleSafe(getValue(row, headerIndex, "GameWeight")))
                .yearPublished(parseIntSafe(getValue(row, headerIndex, "YearPublished")))
                .averageRating(parseDoubleSafe(getValue(row, headerIndex, "AvgRating")))
                .numRatings(parseIntSafe(getValue(row, headerIndex, "NumUserRatings")))
                .description(getValue(row, headerIndex, "Description"))
                .difficulty(mapWeightToDifficulty(parseDoubleSafe(getValue(row, headerIndex, "GameWeight"))))
                .build();
    }

    private String getValue(String[] row, Map<String, Integer> headerIndex, String columnName) {
        Integer idx = headerIndex.get(columnName);
        if (idx == null || idx >= row.length) {
            return null;
        }
        String value = row[idx].trim();
        return value.isEmpty() ? null : value;
    }

    private Long parseLongSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseIntWithDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String mapWeightToDifficulty(Double weight) {
        if (weight == null) return null;
        if (weight < 2.0) return "초급";
        if (weight < 3.0) return "중급";
        if (weight < 4.0) return "고급";
        return "전문가";
    }
}
