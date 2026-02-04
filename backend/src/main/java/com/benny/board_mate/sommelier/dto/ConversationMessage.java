package com.benny.board_mate.sommelier.dto;

import java.time.LocalDateTime;

public record ConversationMessage(
        String role,
        String content,
        LocalDateTime timestamp
) {
    public static ConversationMessage user(String content) {
        return new ConversationMessage("user", content, LocalDateTime.now());
    }

    public static ConversationMessage assistant(String content) {
        return new ConversationMessage("assistant", content, LocalDateTime.now());
    }
}
