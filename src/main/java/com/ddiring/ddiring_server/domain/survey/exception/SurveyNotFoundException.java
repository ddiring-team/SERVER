package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveyNotFoundException extends CustomException {
    public SurveyNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.SURVEY_NOT_FOUND.getMessage());
    }
}
