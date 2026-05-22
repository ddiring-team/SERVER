package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class NotElderRoleException extends CustomException {
    public NotElderRoleException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.NOT_ELDER_ROLE.getMessage());
    }
}
