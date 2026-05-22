package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "설문 생성 응답")
public record CreateSurveyResponse(

        @Schema(description = "생성된 설문 ID", example = "1")
        Long surveyId,

        @Schema(description = "설문 제목", example = "오늘의 안부 설문3")
        String title,

        @Schema(description = "질문 수", example = "3")
        int questionCount
) {
    public static CreateSurveyResponse of(Long surveyId, String title, int questionCount) {
        return new CreateSurveyResponse(surveyId, title, questionCount);
    }
}
