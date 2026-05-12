package com.ddiring.ddiring_server.domain.family.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class NotElderException extends CustomException {
    public NotElderException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.NOT_ELDER.getMessage());
    }
}
