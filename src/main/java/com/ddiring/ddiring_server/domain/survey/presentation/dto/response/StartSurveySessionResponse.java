package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "설문 세션 시작 응답")
public record StartSurveySessionResponse(

        @Schema(description = "세션 ID", example = "1")
        Long sessionId,

        @Schema(description = "설문 ID", example = "10")
        Long surveyId,

        @Schema(description = "설문 제목", example = "오늘의 안부 설문")
        String surveyTitle,

        @Schema(description = "질문 목록 (orderNum 오름차순)")
        List<SurveyQuestionForSessionResponse> questions
) {}
