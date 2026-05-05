package com.ddiring.ddiring_server.domain.family.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    FAMILY_MEMBER_NOT_FOUND("가족방 멤버 정보를 찾을 수 없습니다."),
    ELDER_NOT_IN_FAMILY("해당 가족방의 어르신이 아닙니다.");

    private final String message;
}