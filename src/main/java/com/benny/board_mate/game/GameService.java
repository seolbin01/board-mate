package com.benny.board_mate.game;

import com.benny.board_mate.game.dto.GameResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final GameRepository gameRepository;

    public List<GameResponse> getAllGames() {
        return gameRepository.findAll().stream()
                .map(GameResponse::from)
                .toList();
    }
}