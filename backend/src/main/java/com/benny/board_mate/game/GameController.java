package com.benny.board_mate.game;

import com.benny.board_mate.common.response.ApiResponse;
import com.benny.board_mate.game.dto.GameResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Game", description = "보드게임 - 게임 목록 조회")
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GameResponse>>> getAllGames() {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getAllGames()));
    }
}