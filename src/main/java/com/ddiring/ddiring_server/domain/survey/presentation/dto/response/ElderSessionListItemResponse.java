package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "어르신 설문 세션 목록 항목")
public record ElderSessionListItemResponse(

        @Schema(description = "세션 ID", example = "1")
        Long sessionId,

        @Schema(description = "설문 제목", example = "오늘의 안부 설문")
        String surveyTitle,

        @Schema(description = "설문 날짜", example = "2026-05-22")
        LocalDate sessionDate,

        @Schema(description = "완료 일시", example = "2026-05-22T18:46:00")
        LocalDateTime completedAt
) {
    public static ElderSessionListItemResponse from(SurveySession session) {
        return new ElderSessionListItemResponse(
                session.getId(),
                session.getSurvey().getTitle(),
                session.getSessionDate(),
                session.getCompletedAt()
        );
    }
}
