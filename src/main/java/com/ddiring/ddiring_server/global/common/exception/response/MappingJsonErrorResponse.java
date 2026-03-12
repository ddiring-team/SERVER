package com.ddiring.ddiring_server.global.common.exception.response;

public record MappingJsonErrorResponse(
        String field,
        String message
) implements JsonErrorResponseDetail {
}
