package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class SurveyFamilyNotFoundException extends CustomException {
    public SurveyFamilyNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.NOT_IN_FAMILY.getMessage());
    }
}
