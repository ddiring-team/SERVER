package com.ddiring.ddiring_server.domain.alert.domain.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자가 켜고 끌 수 있는 알림 종류. 발송 이벤트 단위인 {@link AlertType}보다 큰 묶음이며,
 * 알림 설정 화면의 토글 하나에 대응한다.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    ATTENDANCE("출석 완료/미완료 알림"),
    SURVEY("설문 완료/미완료 알림"),
    RISK("이상 징후 알림"),
    DISTANCE("안부거리 알림");

    private final String label;
}
