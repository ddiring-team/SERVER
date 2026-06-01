package com.ddiring.ddiring_server.domain.temperature.application.event;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.enums.TemperatureActionType;

/**
 * 안부 온도 상승 이벤트.
 * actorUserId(활동 주체)의 온도를 actionType에 따라 하루 1회 상승시킨다.
 */
public record TemperatureRaiseEvent(
        Long actorUserId,
        TemperatureActionType actionType
) {
}
