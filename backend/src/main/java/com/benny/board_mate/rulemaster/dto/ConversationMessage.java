package com.benny.board_mate.rulemaster.dto;

import java.time.LocalDateTime;

public record ConversationMessage(
    String role,  // "user" or "assistant"
    String content,
    LocalDateTime timestamp
) {}
