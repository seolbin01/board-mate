package com.benny.board_mate.review;

import com.benny.board_mate.common.config.RedisConfig;
import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.participant.ParticipantRepository;
import com.benny.board_mate.review.dto.ReviewCreateRequest;
import com.benny.board_mate.review.dto.ReviewResponse;
import com.benny.board_mate.review.dto.UserReviewSummary;
import com.benny.board_mate.room.Room;
import com.benny.board_mate.room.RoomRepository;
import com.benny.board_mate.room.RoomStatus;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_USER_RATING, key = "#request.revieweeId")
    public ReviewResponse createReview(Long reviewerId, ReviewCreateRequest request) {
        // 자기 자신한테 리뷰 불가
        if (reviewerId.equals(request.getRevieweeId())) {
            throw new BusinessException(ErrorCode.REVIEW_SELF_NOT_ALLOWED);
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User reviewee = userRepository.findById(request.getRevieweeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // 게임이 종료된 방에서만 리뷰 가능
        if (room.getRoomStatus() != RoomStatus.CLOSED) {
            throw new BusinessException(ErrorCode.REVIEW_ROOM_NOT_CLOSED);
        }

        // 리뷰어가 해당 방의 참가자인지 확인
        if (!participantRepository.existsByRoomAndUser(room, reviewer)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_PARTICIPANT);
        }

        // 리뷰 대상자도 해당 방의 참가자인지 확인
        if (!participantRepository.existsByRoomAndUser(room, reviewee)) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_PARTICIPANT);
        }

        // 중복 리뷰 체크
        if (reviewRepository.existsByReviewerIdAndRevieweeIdAndRoomId(
                reviewerId, request.getRevieweeId(), request.getRoomId())) {
            throw new BusinessException(ErrorCode.REVIEW_DUPLICATE);
        }

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .room(room)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        return ReviewResponse.from(review);
    }

    @Cacheable(value = RedisConfig.CACHE_USER_RATING, key = "#userId")
    public UserReviewSummary getUserReviews(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<ReviewResponse> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ReviewResponse::from)
                .toList();

        Double averageRating = reviewRepository.getAverageRatingByUserId(userId);
        long reviewCount = reviewRepository.countByRevieweeId(userId);

        return UserReviewSummary.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .averageRating(averageRating != null ? Math.round(averageRating * 10) / 10.0 : null)
                .reviewCount(reviewCount)
                .reviews(reviews)
                .build();
    }
}
