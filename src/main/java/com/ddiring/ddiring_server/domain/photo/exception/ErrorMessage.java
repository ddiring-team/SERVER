package com.ddiring.ddiring_server.domain.photo.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    DAILY_PHOTO_NOT_FOUND("해당 게시글을 찾을 수 없습니다."),
    ALREADY_POSTED_TODAY("오늘 이미 일상을 공유했습니다."),
    PHOTO_ACCESS_DENIED("해당 게시글에 접근할 권한이 없습니다.");

    private final String message;
}