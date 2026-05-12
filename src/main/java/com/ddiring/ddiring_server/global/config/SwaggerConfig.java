package com.ddiring.ddiring_server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
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
                return "https://api.ddiringapp.com";
            }
            if ("dev".equals(profile)) {
                return "https://api.ddiringapp.com";
            }
        }
        // 로컬 환경
        return "http://localhost:8080";
    }

    @Bean
    public OpenApiCustomizer kakaoLoginCustomizer() {
        return openApi -> {
            openApi.addTagsItem(new Tag().name("카카오 로그인").description("카카오 OAuth2 소셜 로그인 흐름"));

            Operation operation = new Operation()
                    .addTagsItem("카카오 로그인")
                    .summary("카카오 로그인 시작")
                    .description("""
                            카카오 OAuth2 인증을 시작합니다. 브라우저(또는 WebView)로 직접 접근하면 카카오 로그인 페이지로 리다이렉트됩니다.

                            **React Native 연동 흐름**
                            1. WebView로 이 URL 열기 (`redirect_url` 파라미터에 딥링크 전달)
                            2. 카카오 로그인 완료
                            3. `{redirect_url}?token={JWT}&isNewUser=true/false` 로 리다이렉트
                            4. `isNewUser=true` → 역할 선택 후 프로필 완성 API 호출
                            5. `isNewUser=false` → 홈 화면 이동
                            """)
                    .addParametersItem(new Parameter()
                            .in("query")
                            .name("redirect_url")
                            .description("인증 완료 후 JWT를 전달받을 딥링크 URL (예: `ddiring://auth`)")
                            .required(false)
                            .schema(new Schema<String>().type("string").example("ddiring://auth")))
                    .responses(new ApiResponses()
                            .addApiResponse("302", new ApiResponse()
                                    .description("카카오 로그인 페이지로 리다이렉트"))
                            .addApiResponse("302 (완료 후)", new ApiResponse()
                                    .description("redirect_url?token={JWT}&isNewUser={true|false} 로 리다이렉트")
                                    .content(new Content().addMediaType("application/json",
                                            new MediaType().schema(new Schema<>()
                                                    .addProperty("token", new Schema<String>().type("string").description("JWT 액세스 토큰"))
                                                    .addProperty("isNewUser", new Schema<Boolean>().type("boolean").description("최초 로그인 여부"))
                                            )))));

            openApi.getPaths().addPathItem("/oauth2/authorization/kakao", new PathItem().get(operation));
        };
    }

    private Info apiInfo() {
        return new Info()
                .title("DDIRING API")
                .description("띠링 서비스의 REST API 문서입니다.")
                .version("1.0.0");
    }

}
