package com.ddiring.ddiring_server.global.common.exception.response;

public record ParseJsonErrorResponse(
        Integer line,
        Integer column,
        String message
) implements JsonErrorResponseDetail {
}
