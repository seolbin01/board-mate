package com.benny.board_mate.rulemaster.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationSession(
    Long userId,
    Long bggId,
    String gameName,
    List<ConversationMessage> messages,
    LocalDateTime createdAt,
    LocalDateTime lastAccessedAt
) {}
