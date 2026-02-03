package com.benny.board_mate.rulemaster.dto;

public record GameSearchResponse(
    Long bggId,
    String name,
    Integer yearPublished,
    String thumbnailUrl
) {}
