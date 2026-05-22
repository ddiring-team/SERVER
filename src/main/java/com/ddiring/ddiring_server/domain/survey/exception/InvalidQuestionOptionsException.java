package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class InvalidQuestionOptionsException extends CustomException {
    public InvalidQuestionOptionsException() {
        super(HttpStatus.BAD_REQUEST, ErrorMessage.INVALID_QUESTION_OPTIONS.getMessage());
    }
}
