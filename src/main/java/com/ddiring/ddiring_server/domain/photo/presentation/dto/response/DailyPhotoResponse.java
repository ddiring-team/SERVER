package com.ddiring.ddiring_server.domain.photo.presentation.dto.response;

import com.ddiring.ddiring_server.domain.photo.domain.entity.DailyPhoto;
import com.ddiring.ddiring_server.domain.photo.domain.entity.enums.EmojiType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "일상 공유 게시글 응답")
public record DailyPhotoResponse(
        @Schema(description = "게시글 ID", example = "1")
        Long id,

        @Schema(description = "작성자 이름", example = "김현수")
        String authorName,

        @Schema(description = "사진 URL", example = "https://bucket.s3.amazonaws.com/photo.jpg")
        String photoUrl,

        @Schema(description = "사진 설명", example = "좋은 기분이에요~")
        String caption,

        @Schema(description = "게시 날짜", example = "2026-05-09")
        LocalDate takenDate,

        @Schema(description = "작성 시각", example = "2026-05-09T11:23:00")
        LocalDateTime createdAt,

        @Schema(description = "이모지 반응 목록 (HEART, SMILE, LAUGH, CRY 순)")
        List<ReactionCount> reactions
) {
    @Schema(description = "이모지 반응 집계")
    public record ReactionCount(
            @Schema(description = "이모지 타입", example = "HEART")
            EmojiType emojiType,

            @Schema(description = "반응 수", example = "3")
            long count,

            @Schema(description = "현재 유저의 반응 여부", example = "true")
            boolean myReaction
    ) {}

    public static DailyPhotoResponse of(DailyPhoto photo, List<ReactionCount> reactions) {
        return new DailyPhotoResponse(
                photo.getId(),
                photo.getUser().getName(),
                photo.getPhotoUrl(),
                photo.getCaption(),
                photo.getTakenDate(),
                photo.getCreatedAt(),
                reactions
        );
    }
}