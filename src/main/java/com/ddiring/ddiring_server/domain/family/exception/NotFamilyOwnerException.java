package com.ddiring.ddiring_server.domain.family.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class NotFamilyOwnerException extends CustomException {
    public NotFamilyOwnerException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.NOT_FAMILY_OWNER.getMessage());
    }
}
