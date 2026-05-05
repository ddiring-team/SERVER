package com.ddiring.ddiring_server.domain.family.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class FamilyMemberNotFoundException extends CustomException {
    public FamilyMemberNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.FAMILY_MEMBER_NOT_FOUND.getMessage());
    }
}