package com.ddiring.ddiring_server.domain.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "어르신 로그인 요청")
public record ElderLoginRequest(

        @Schema(description = "가족방 초대코드 (6자리 대문자+숫자)", example = "ABC123")
        @NotBlank
        @Size(min = 6, max = 6)
        String inviteCode,

        @Schema(description = "어르신 이름", example = "김어르신")
        @NotBlank
        String name
) {}
