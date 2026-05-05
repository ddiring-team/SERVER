package com.ddiring.ddiring_server.domain.auth.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ElderNotFoundException extends CustomException {
    public ElderNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.ELDER_NOT_FOUND.getMessage());
    }
}
