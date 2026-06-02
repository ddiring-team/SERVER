package com.ddiring.ddiring_server.domain.alert.presentation.dto.response;

import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 설정 항목")
public record NotificationSettingResponse(
        @Schema(description = "알림 타입", example = "ATTENDANCE")
        NotificationType type,

        @Schema(description = "화면 표시용 라벨", example = "출석 완료/미완료 알림")
        String label,

        @Schema(description = "알림 수신 여부", example = "true")
        boolean enabled
) {
    public static NotificationSettingResponse of(NotificationType type, boolean enabled) {
        return new NotificationSettingResponse(type, type.getLabel(), enabled);
    }
}
