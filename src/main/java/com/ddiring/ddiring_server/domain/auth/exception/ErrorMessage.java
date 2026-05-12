package com.ddiring.ddiring_server.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    DUPLICATE_LOGIN_ID("이미 사용 중인 아이디입니다."),
    DUPLICATE_PHONE("이미 사용 중인 전화번호입니다."),
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다."),
    ELDER_NOT_FOUND("해당 가족방에서 어르신 정보를 찾을 수 없습니다."),
    ELDER_ALREADY_REGISTERED("해당 이름으로 이미 등록된 어르신이 있습니다."),
    KAKAO_USER_NOT_FOUND("카카오 사용자 정보를 찾을 수 없습니다."),
    PROFILE_INCOMPLETE("프로필 설정이 완료되지 않았습니다.");

    private final String message;
}
