package com.ddiring.ddiring_server.domain.auth.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답")
public record AuthResponse(

        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzUxMiJ9...")
        String token,

        @Schema(description = "사용자 역할", example = "GUARDIAN", allowableValues = {"GUARDIAN", "ELDER"})
        String role
) {
    public static AuthResponse of(String token, String role) {
        return new AuthResponse(token, role);
    }
}
