package com.ddiring.ddiring_server.domain.photo.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "가족방 피드 무한 스크롤 응답")
public record DailyPhotoFeedResponse(
        @Schema(description = "게시글 목록")
        List<DailyPhotoResponse> photos,

        @Schema(description = "다음 페이지 커서 (마지막 게시글 ID). null이면 마지막 페이지", example = "42")
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {}
