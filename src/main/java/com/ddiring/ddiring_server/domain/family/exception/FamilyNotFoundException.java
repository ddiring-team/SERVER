package com.ddiring.ddiring_server.domain.family.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class FamilyNotFoundException extends CustomException {
    public FamilyNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.FAMILY_NOT_FOUND.getMessage());
    }
}
