package com.ddiring.ddiring_server.domain.family.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AlreadyInFamilyException extends CustomException {
    public AlreadyInFamilyException() {
        super(HttpStatus.CONFLICT, ErrorMessage.ALREADY_IN_FAMILY.getMessage());
    }
}
