package com.ddiring.ddiring_server.domain.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "카카오 보호자 프로필 완성 요청")
public record KakaoGuardianCompleteRequest(

        @Schema(description = "이름", example = "홍길동")
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(description = "전화번호 (숫자만)", example = "01012345678")
        @NotBlank
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phone,

        @Schema(description = "생년월일", example = "1990-01-01")
        @NotNull
        LocalDate birthDate
) {
}
