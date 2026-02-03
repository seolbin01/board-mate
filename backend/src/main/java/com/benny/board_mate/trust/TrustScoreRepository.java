package com.benny.board_mate.trust;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrustScoreRepository extends JpaRepository<TrustScore, Long> {

    Optional<TrustScore> findByUserId(Long userId);
}