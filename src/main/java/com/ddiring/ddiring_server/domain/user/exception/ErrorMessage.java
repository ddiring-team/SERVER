package com.ddiring.ddiring_server.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    USER_NOT_FOUND("사용자를 찾을 수 없습니다.");

    private final String message;
}