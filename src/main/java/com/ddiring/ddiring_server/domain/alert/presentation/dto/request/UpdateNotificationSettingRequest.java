package com.ddiring.ddiring_server.domain.alert.presentation.dto.request;

import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "알림 설정 변경 요청")
public record UpdateNotificationSettingRequest(
        @Schema(description = "변경할 알림 타입", example = "SURVEY", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "알림 타입은 필수입니다.")
        NotificationType type,

        @Schema(description = "알림 수신 여부 (true=켜기, false=끄기)", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "수신 여부는 필수입니다.")
        Boolean enabled
) {
}
