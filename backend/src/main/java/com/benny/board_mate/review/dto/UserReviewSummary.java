package com.benny.board_mate.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserReviewSummary {

    private Long userId;
    private String nickname;
    private Double averageRating;
    private long reviewCount;
    private List<ReviewResponse> reviews;
}
