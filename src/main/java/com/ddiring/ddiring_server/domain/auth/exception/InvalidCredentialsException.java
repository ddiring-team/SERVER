package com.ddiring.ddiring_server.domain.auth.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends CustomException {
    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, ErrorMessage.INVALID_CREDENTIALS.getMessage());
    }
}
