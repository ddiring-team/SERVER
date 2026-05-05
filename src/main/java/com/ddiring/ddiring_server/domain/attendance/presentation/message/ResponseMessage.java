package com.ddiring.ddiring_server.domain.attendance.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    ATTENDANCE_CHECK_IN_SUCCESS("출석 체크가 완료되었습니다."),
    ATTENDANCE_TODAY_SUCCESS("오늘의 출석 여부를 조회했습니다."),
    ATTENDANCE_MONTHLY_SUCCESS("월별 출석 현황을 조회했습니다.");

    private final String message;
}