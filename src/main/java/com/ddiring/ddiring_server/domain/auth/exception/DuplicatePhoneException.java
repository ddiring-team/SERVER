package com.ddiring.ddiring_server.domain.auth.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicatePhoneException extends CustomException {
    public DuplicatePhoneException() {
        super(HttpStatus.CONFLICT, ErrorMessage.DUPLICATE_PHONE.getMessage());
    }
}
