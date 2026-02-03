package com.benny.board_mate.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient bggWebClient() {
        return WebClient.builder()
                .baseUrl("https://boardgamegeek.com/xmlapi2")
                .defaultHeader("User-Agent", "BoardMate/1.0 (board-mate.vercel.app)")
                .defaultHeader("Accept", "application/xml")
                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
