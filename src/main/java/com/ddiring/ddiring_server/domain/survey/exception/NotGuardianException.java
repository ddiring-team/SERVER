package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class NotGuardianException extends CustomException {
    public NotGuardianException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.NOT_GUARDIAN.getMessage());
    }
}
