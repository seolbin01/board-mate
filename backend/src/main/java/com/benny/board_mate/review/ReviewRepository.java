package com.benny.board_mate.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 특정 유저가 받은 리뷰 목록
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    // 중복 리뷰 체크
    boolean existsByReviewerIdAndRevieweeIdAndRoomId(Long reviewerId, Long revieweeId, Long roomId);

    // 평균 별점 계산
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.reviewee.id = :userId")
    Double getAverageRatingByUserId(@Param("userId") Long userId);

    // 리뷰 개수
    long countByRevieweeId(Long revieweeId);
}
