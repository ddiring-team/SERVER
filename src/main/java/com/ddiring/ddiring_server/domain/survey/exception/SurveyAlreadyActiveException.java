package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveyAlreadyActiveException extends CustomException {
    public SurveyAlreadyActiveException() {
        super(HttpStatus.BAD_REQUEST, ErrorMessage.SURVEY_ALREADY_ACTIVE.getMessage());
    }
}
