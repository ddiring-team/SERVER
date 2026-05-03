package com.ddiring.ddiring_server.global.storage.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    INVALID_MIME_TYPE("올바르지 않은 mime type 입니다.");

    private final String message;
}
