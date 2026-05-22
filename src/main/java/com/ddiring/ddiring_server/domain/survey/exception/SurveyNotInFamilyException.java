package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveyNotInFamilyException extends CustomException {
    public SurveyNotInFamilyException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.SURVEY_NOT_IN_FAMILY.getMessage());
    }
}
