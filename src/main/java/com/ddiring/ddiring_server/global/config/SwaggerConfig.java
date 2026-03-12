package com.ddiring.ddiring_server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;


import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String JWT_SCHEME = "jwtAuth";

    private final Environment environment;

    public SwaggerConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public OpenAPI openAPI() {
        // 현재 실행 환경에 맞는 서버 URL 설정
        String serverUrl = getServerUrl();

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(new Server().url(serverUrl).description("API Server")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(JWT_SCHEME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                        )
                );
    }

    private String getServerUrl() {
        // 프로덕션 환경에서는 HTTPS 사용
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("prod".equals(profile)) {
                return "https://api.ddiring.com";
            }
            if ("dev".equals(profile)) {
                return "https://api.ddiring.com";
            }
        }
        // 로컬 환경
        return "http://localhost:8080";
    }

    private Info apiInfo() {
        return new Info()
                .title("DDIRING API")
                .description("띠링 서비스의 REST API 문서입니다.")
                .version("1.0.0");
    }

}
