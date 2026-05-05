package com.ddiring.ddiring_server.domain.auth.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ElderAlreadyRegisteredException extends CustomException {
    public ElderAlreadyRegisteredException() {
        super(HttpStatus.CONFLICT, ErrorMessage.ELDER_ALREADY_REGISTERED.getMessage());
    }
}
