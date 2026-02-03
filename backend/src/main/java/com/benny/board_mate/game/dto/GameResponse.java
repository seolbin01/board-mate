package com.benny.board_mate.game.dto;

import com.benny.board_mate.game.BoardGame;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameResponse {

    private Long id;
    private String title;
    private int minPlayers;
    private int maxPlayers;
    private Integer playtime;
    private String difficulty;
    private String imageUrl;
    private String description;

    public static GameResponse from(BoardGame game) {
        return GameResponse.builder()
                .id(game.getId())
                .title(game.getTitle())
                .minPlayers(game.getMinPlayers())
                .maxPlayers(game.getMaxPlayers())
                .playtime(game.getPlaytime())
                .difficulty(game.getDifficulty())
                .imageUrl(game.getImageUrl())
                .description(game.getDescription())
                .build();
    }
}