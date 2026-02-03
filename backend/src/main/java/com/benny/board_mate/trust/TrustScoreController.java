package com.benny.board_mate.trust;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.trust.dto.TrustScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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