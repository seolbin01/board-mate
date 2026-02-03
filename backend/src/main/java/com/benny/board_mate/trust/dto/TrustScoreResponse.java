package com.benny.board_mate.trust.dto;

import com.benny.board_mate.trust.TrustScore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrustScoreResponse {

    private Long userId;
    private int score;
    private int totalParticipations;
    private int attendedCount;
    private int noShowCount;
    private String grade;

    public static TrustScoreResponse from(TrustScore trustScore) {
        return TrustScoreResponse.builder()
                .userId(trustScore.getUser().getId())
                .score(trustScore.getScore())
                .totalParticipations(trustScore.getTotalParticipations())
                .attendedCount(trustScore.getAttendedCount())
                .noShowCount(trustScore.getNoShowCount())
                .grade(calculateGrade(trustScore.getScore()))
                .build();
    }

    private static String calculateGrade(int score) {
        if (score >= 150) return "S";
        if (score >= 120) return "A";
        if (score >= 100) return "B";
        if (score >= 70) return "C";
        if (score >= 40) return "D";
        return "F";
    }
}