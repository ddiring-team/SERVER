package com.ddiring.ddiring_server.domain.user.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {
    FCM_TOKEN_UPDATED("FCM 토큰이 등록되었습니다.");

    private final String message;
}
