package com.benny.board_mate.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${swagger.server-url:}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("BoardMate API")
                        .description("보드게임 모임 매칭 플랫폼 API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BoardMate Team")
                                .email("support@boardmate.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요 (Bearer 없이 토큰만)")));

        // 프로덕션 환경에서 HTTPS 서버 URL 설정
        if (serverUrl != null && !serverUrl.isEmpty()) {
            openAPI.servers(List.of(new Server().url(serverUrl).description("Production")));
        }

        return openAPI;
    }
}
