package com.ddiring.ddiring_server.domain.alert.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {
    NOTIFICATION_SETTINGS_FETCHED("알림 설정을 조회했습니다."),
    NOTIFICATION_SETTING_UPDATED("알림 설정을 변경했습니다.");

    private final String message;
}
