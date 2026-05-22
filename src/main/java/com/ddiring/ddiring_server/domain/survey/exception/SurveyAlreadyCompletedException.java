package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveyAlreadyCompletedException extends CustomException {
    public SurveyAlreadyCompletedException() {
        super(HttpStatus.CONFLICT, ErrorMessage.SURVEY_ALREADY_COMPLETED.getMessage());
    }
}
