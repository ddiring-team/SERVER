package com.ddiring.ddiring_server.domain.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "보호자 로그인 요청")
public record GuardianLoginRequest(

        @Schema(description = "로그인 아이디", example = "guardian01")
        @NotBlank
        String loginId,

        @Schema(description = "비밀번호", example = "password123")
        @NotBlank
        String password
) {}
