package com.benny.board_mate.game;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<BoardGame, Long> {

    Optional<BoardGame> findByBggId(Long bggId);

    @Query("SELECT g FROM BoardGame g WHERE " +
           "LOWER(g.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(g.titleKorean) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY g.numRatings DESC NULLS LAST")
    List<BoardGame> searchByTitle(@Param("query") String query, Pageable pageable);

    List<BoardGame> findTop100ByOrderByNumRatingsDesc();
}