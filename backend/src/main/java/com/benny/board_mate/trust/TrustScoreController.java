package com.benny.board_mate.trust;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.trust.dto.TrustScoreResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "TrustScore", description = "신뢰 점수 - 사용자 평판 조회")
@RestController
@RequestMapping("/api/trust-scores")
@RequiredArgsConstructor
public class TrustScoreController {

    private final TrustScoreService trustScoreService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<TrustScoreResponse>> getMyScore(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(trustScoreService.getScore(userId)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<TrustScoreResponse>> getScore(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(trustScoreService.getScore(userId)));
    }
}