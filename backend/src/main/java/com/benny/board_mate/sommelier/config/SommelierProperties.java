package com.benny.board_mate.sommelier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sommelier")
public record SommelierProperties(
        Gemini gemini,
        Conversation conversation
) {
    public record Gemini(
            String apiKey,
            String model,
            int maxTokens,
            double temperature,
            int timeout
    ) {}

    public record Conversation(
            int maxHistory,
            int ttlHours
    ) {}
}
