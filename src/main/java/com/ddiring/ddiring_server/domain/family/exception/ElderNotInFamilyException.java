package com.ddiring.ddiring_server.domain.family.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class ElderNotInFamilyException extends CustomException {
    public ElderNotInFamilyException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.ELDER_NOT_IN_FAMILY.getMessage());
    }
}