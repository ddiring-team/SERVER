package com.ddiring.ddiring_server.domain.auth.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class KakaoUserNotFoundException extends CustomException {
    public KakaoUserNotFoundException() {
        super(HttpStatus.UNAUTHORIZED, ErrorMessage.KAKAO_USER_NOT_FOUND.getMessage());
    }
}
