package com.ddiring.ddiring_server.global.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomException extends RuntimeException {
    private final HttpStatus status;

    public CustomException(final HttpStatus status, final String message) {
        super(message);
        this.status = status;
    }
}
