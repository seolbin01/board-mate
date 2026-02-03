package com.benny.board_mate.review;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.review.dto.ReviewCreateRequest;
import com.benny.board_mate.review.dto.ReviewResponse;
import com.benny.board_mate.review.dto.UserReviewSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            Authentication authentication,
            @Valid @RequestBody ReviewCreateRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        ReviewResponse response = reviewService.createReview(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<ApiResponse<UserReviewSummary>> getUserReviews(
            @PathVariable Long userId) {
        UserReviewSummary summary = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
