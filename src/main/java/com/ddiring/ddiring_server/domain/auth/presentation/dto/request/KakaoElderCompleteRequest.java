package com.ddiring.ddiring_server.domain.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


import java.time.LocalDate;

@Schema(description = "카카오 어르신 프로필 완성 요청")
public record KakaoElderCompleteRequest(

        @Schema(description = "이름", example = "김순자")
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(description = "전화번호 (숫자만)", example = "01098765432")
        @NotBlank
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 전화번호 형식이 아닙니다.")
        String phone,

        @Schema(description = "생년월일", example = "1950-05-05")
        @NotNull
        LocalDate birthDate
) {
}
