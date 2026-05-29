package com.ddiring.ddiring_server.domain.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "보호자 회원가입 요청")
public record GuardianSignupRequest(

        @Schema(description = "로그인 아이디 (4~20자)", example = "guardian01")
        @NotBlank
        @Size(min = 4, max = 20)
        String loginId,

        @Schema(description = "비밀번호 (8~20자)", example = "password123")
        @NotBlank
        @Size(min = 8, max = 20)
        String password,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(description = "전화번호", example = "01012345678")
        @NotBlank
        @Size(max = 20)
        String phone,

        @Schema(description = "생년월일", example = "1990-01-15")
        @NotNull
        LocalDate birthDate
) {}
