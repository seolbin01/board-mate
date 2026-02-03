package com.benny.board_mate.game;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<BoardGame, Long> {
}