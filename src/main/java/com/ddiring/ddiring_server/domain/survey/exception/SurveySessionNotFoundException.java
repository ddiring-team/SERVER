package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveySessionNotFoundException extends CustomException {
    public SurveySessionNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.SURVEY_SESSION_NOT_FOUND.getMessage());
    }
}
