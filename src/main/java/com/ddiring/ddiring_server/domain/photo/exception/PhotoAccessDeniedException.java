package com.ddiring.ddiring_server.domain.photo.exception;

import com.ddiring.ddiring_server.global.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public class PhotoAccessDeniedException extends CustomException {
    public PhotoAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, ErrorMessage.PHOTO_ACCESS_DENIED.getMessage());
    }
}
