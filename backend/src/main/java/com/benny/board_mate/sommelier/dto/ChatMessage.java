package com.benny.board_mate.sommelier.dto;

import java.time.LocalDateTime;

public record ChatMessage(
    String role,
    String content,
    LocalDateTime timestamp
) {
    public static ChatMessage user(String content) {
        return new ChatMessage("user", content, LocalDateTime.now());
    }

    public static ChatMessage assistant(String content) {
        return new ChatMessage("assistant", content, LocalDateTime.now());
    }
}
