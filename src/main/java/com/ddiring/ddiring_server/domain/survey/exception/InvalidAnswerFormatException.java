package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidAnswerFormatException extends CustomException {
    public InvalidAnswerFormatException() {
        super(HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_ANSWER_FORMAT.getMessage());
    }
}
