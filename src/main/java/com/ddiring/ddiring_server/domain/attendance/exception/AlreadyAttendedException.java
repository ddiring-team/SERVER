package com.ddiring.ddiring_server.domain.attendance.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class AlreadyAttendedException extends CustomException {
    public AlreadyAttendedException() {
        super(HttpStatus.CONFLICT, ErrorMessage.ALREADY_ATTENDED.getMessage());
    }
}
