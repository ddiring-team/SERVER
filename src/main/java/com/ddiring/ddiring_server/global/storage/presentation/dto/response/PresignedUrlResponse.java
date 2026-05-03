package com.ddiring.ddiring_server.global.storage.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 발급 응답")
public record PresignedUrlResponse(
        @Schema(description = "S3 Presigned URL (10분 유효)",
                example = "https://bucket.s3.ap-northeast-2.amazonaws.com/image/uuid.jpeg?X-Amz-Algorithm=...")
        String presignedUrl
) {
}
