package com.ddiring.ddiring_server.domain.alert.domain.entity.enums;

public enum AlertType {
    ATTENDANCE_INACTIVE,   // 출석 3일 이상 미진행
    WEEKLY_RISK,           // 주간 요약 위험 패턴 감지
    DISTANCE_MAX_REACHED   // 안부거리 10km 도달
}
