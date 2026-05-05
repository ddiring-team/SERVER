package com.ddiring.ddiring_server.domain.auth.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateLoginIdException extends CustomException {
    public DuplicateLoginIdException() {
        super(HttpStatus.CONFLICT, ErrorMessage.DUPLICATE_LOGIN_ID.getMessage());
    }
}
