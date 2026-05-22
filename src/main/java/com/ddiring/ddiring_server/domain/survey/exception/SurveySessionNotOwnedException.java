package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveySessionNotOwnedException extends CustomException {
    public SurveySessionNotOwnedException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.SURVEY_SESSION_NOT_OWNED.getMessage());
    }
}
