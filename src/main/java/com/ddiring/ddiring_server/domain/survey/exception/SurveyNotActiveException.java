package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveyNotActiveException extends CustomException {
    public SurveyNotActiveException() {
        super(HttpStatus.CONFLICT, ErrorMessage.SURVEY_NOT_ACTIVE.getMessage());
    }
}
