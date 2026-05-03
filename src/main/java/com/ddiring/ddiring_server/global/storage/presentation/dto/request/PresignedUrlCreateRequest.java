package com.ddiring.ddiring_server.global.storage.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Presigned URL 발급 요청")
public record PresignedUrlCreateRequest(
        @Schema(description = "업로드할 파일의 MIME 타입", example = "image/jpeg",
                allowableValues = {"image/jpg", "image/jpeg", "image/png", "image/webp"})
        String mimeType
){
}
