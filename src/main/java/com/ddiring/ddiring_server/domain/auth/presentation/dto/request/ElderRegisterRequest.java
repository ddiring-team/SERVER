package com.ddiring.ddiring_server.domain.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "어르신 최초 등록 요청")
public record ElderRegisterRequest(

        @Schema(description = "가족방 초대코드 (6자리 대문자+숫자)", example = "ABC123")
        @NotBlank
        @Size(min = 6, max = 6)
        String inviteCode,

        @Schema(description = "어르신 이름", example = "김어르신")
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(description = "전화번호", example = "01012345678")
        @NotBlank
        @Size(max = 20)
        String phone,

        @Schema(description = "생년월일 (선택)", example = "1950-03-15", nullable = true)
        LocalDate birthDate
) {}
