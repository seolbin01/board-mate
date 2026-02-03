package com.benny.board_mate.rulemaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rulemaster")
public record RuleMasterProperties(
    Gemini gemini,
    Bgg bgg,
    Conversation conversation
) {
    public record Gemini(
        String apiKey,
        String model,
        int maxTokens,
        double temperature,
        int timeout
    ) {}

    public record Bgg(
        String baseUrl,
        int timeout
    ) {}

    public record Conversation(
        int maxHistory,
        int ttlHours
    ) {}
}
