package com.benny.board_mate.trust;

import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.trust.dto.TrustScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrustScoreService {

    private final TrustScoreRepository trustScoreRepository;

    public TrustScoreResponse getScore(Long userId) {
        TrustScore trustScore = trustScoreRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return TrustScoreResponse.from(trustScore);
    }

    @Transactional
    public void addAttendance(Long userId) {
        TrustScore trustScore = trustScoreRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        trustScore.addAttendance();
    }

    @Transactional
    public void addNoShow(Long userId) {
        TrustScore trustScore = trustScoreRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        trustScore.addNoShow();
    }
}