package com.ddiring.ddiring_server.domain.survey.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DuplicateOrderNumException extends CustomException {
    public DuplicateOrderNumException() {
        super(HttpStatus.BAD_REQUEST, ErrorMessage.DUPLICATE_ORDER_NUM.getMessage());
    }
}
