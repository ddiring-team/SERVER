package com.ddiring.ddiring_server.global.storage.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidMimeTypeException extends CustomException {
    public InvalidMimeTypeException() {
        super(HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_MIME_TYPE.getMessage());
    }
}
