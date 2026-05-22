package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveyNotOwnedException extends CustomException {
    public SurveyNotOwnedException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.SURVEY_NOT_OWNED.getMessage());
    }
}
