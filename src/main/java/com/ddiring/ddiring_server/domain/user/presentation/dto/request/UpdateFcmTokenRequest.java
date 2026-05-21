package com.ddiring.ddiring_server.domain.user.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FCM 토큰 등록 요청")
public record UpdateFcmTokenRequest(
        @Schema(description = "Firebase Cloud Messaging 기기 토큰", example = "eXaMpLeFcMtOkEn...")
        String fcmToken
) {
}
