package com.ddiring.ddiring_server.domain.photo.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class DailyPhotoNotFoundException extends CustomException {
    public DailyPhotoNotFoundException() {
        super(HttpStatus.NOT_FOUND, ErrorMessage.DAILY_PHOTO_NOT_FOUND.getMessage());
    }
}