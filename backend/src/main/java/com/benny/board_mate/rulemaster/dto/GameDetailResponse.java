package com.benny.board_mate.rulemaster.dto;

import java.util.List;

public record GameDetailResponse(
    Long bggId,
    String name,
    String nameKorean,
    Integer yearPublished,
    String description,
    Integer minPlayers,
    Integer maxPlayers,
    Integer playingTime,
    Integer minPlayTime,
    Integer maxPlayTime,
    List<String> mechanics,
    List<String> categories,
    String imageUrl,
    String thumbnailUrl,
    Double averageRating,
    Double weight
) {}
