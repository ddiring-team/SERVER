package com.ddiring.ddiring_server.domain.temperature.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    TEMPERATURE_GET_SUCCESS("안부 온도를 조회했습니다."),
    WEEKLY_TEMPERATURE_SUCCESS("주간 안부 온도 변화를 조회했습니다.");

    private final String message;
}
