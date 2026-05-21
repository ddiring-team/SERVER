package com.ddiring.ddiring_server.domain.user.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "FCM 토큰 등록 요청")
public record UpdateFcmTokenRequest(
        @NotBlank(message = "FCM 토큰은 필수입니다.")
        @Schema(description = "Firebase Cloud Messaging 기기 토큰", example = "eXaMpLeFcMtOkEn...")
        String fcmToken
) {
}
