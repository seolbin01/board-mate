package com.benny.board_mate.sommelier.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SommelierRequest(
    @NotNull String sessionId,
    @NotBlank @Size(max = 1000) String message
) {}
