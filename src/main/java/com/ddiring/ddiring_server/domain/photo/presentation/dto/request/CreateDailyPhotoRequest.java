package com.ddiring.ddiring_server.domain.photo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "일상 공유 게시글 작성 요청")
public record CreateDailyPhotoRequest(
        @Schema(description = "S3 업로드 후 발급된 이미지 URL", example = "https://bucket.s3.amazonaws.com/photo.jpg")
        @NotBlank String photoUrl,

        @Schema(description = "사진 설명 텍스트", example = "좋은 기분이에요~")
        @NotBlank String caption
) {}