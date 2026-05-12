package com.ddiring.ddiring_server.domain.family.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    FAMILY_MEMBER_NOT_FOUND("가족방 멤버 정보를 찾을 수 없습니다."),
    ELDER_NOT_IN_FAMILY("해당 가족방의 어르신이 아닙니다."),
    ALREADY_IN_FAMILY("이미 가족방에 소속되어 있습니다."),
    FAMILY_NOT_FOUND("가족방을 찾을 수 없습니다."),
    NOT_FAMILY_OWNER("방 생성자만 수행할 수 있습니다."),
    NOT_ELDER("어르신만 가족방에 입장할 수 있습니다.");

    private final String message;
}