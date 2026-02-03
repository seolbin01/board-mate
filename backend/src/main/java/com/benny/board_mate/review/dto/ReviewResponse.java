package com.benny.board_mate.review.dto;

import com.benny.board_mate.review.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {

    private Long id;
    private Long reviewerId;
    private String reviewerNickname;
    private Long roomId;
    private String roomRegion;
    private String gameTitle;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .reviewerId(review.getReviewer().getId())
                .reviewerNickname(review.getReviewer().getNickname())
                .roomId(review.getRoom().getId())
                .roomRegion(review.getRoom().getRegion())
                .gameTitle(review.getRoom().getGame().getTitle())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
