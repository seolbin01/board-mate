package com.benny.board_mate.rulemaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @NotNull Long bggId,
    @NotBlank @Size(max = 1000) String message
) {}
