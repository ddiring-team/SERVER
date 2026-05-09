package com.ddiring.ddiring_server.domain.photo.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AlreadyPostedTodayException extends CustomException {
    public AlreadyPostedTodayException() {
        super(HttpStatus.CONFLICT, ErrorMessage.ALREADY_POSTED_TODAY.getMessage());
    }
}