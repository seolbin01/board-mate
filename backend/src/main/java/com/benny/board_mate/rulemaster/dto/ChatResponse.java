package com.benny.board_mate.rulemaster.dto;

public record ChatResponse(
    String type,  // "content", "done", "error"
    String content,
    String messageId,
    ErrorDetail error
) {
    public record ErrorDetail(String code, String message, boolean retryable) {}

    public static ChatResponse content(String content) {
        return new ChatResponse("content", content, null, null);
    }

    public static ChatResponse done(String messageId) {
        return new ChatResponse("done", null, messageId, null);
    }

    public static ChatResponse error(String code, String message, boolean retryable) {
        return new ChatResponse("error", null, null, new ErrorDetail(code, message, retryable));
    }
}
