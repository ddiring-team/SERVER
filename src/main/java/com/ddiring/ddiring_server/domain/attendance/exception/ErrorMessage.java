package com.ddiring.ddiring_server.domain.attendance.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    ALREADY_ATTENDED("이미 오늘 출석 체크를 완료했습니다.");

    private final String message;
}
