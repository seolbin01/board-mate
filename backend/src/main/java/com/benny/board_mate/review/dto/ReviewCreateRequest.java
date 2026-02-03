package com.benny.board_mate.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewCreateRequest {

    @NotNull(message = "방 ID는 필수입니다")
    private Long roomId;

    @NotNull(message = "리뷰 대상 유저 ID는 필수입니다")
    private Long revieweeId;

    @Min(value = 1, message = "별점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "별점은 5점 이하여야 합니다")
    private int rating;

    @Size(max = 500, message = "코멘트는 500자 이하여야 합니다")
    private String comment;
}
