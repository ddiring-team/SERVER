package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import com.ddiring.ddiring_server.domain.survey.domain.entity.Survey;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "설문 목록 항목")
public record SurveyListItemResponse(

        @Schema(description = "설문 ID", example = "1")
        Long surveyId,

        @Schema(description = "설문 제목", example = "오늘의 안부 설문")
        String title,

        @Schema(description = "질문 수", example = "5")
        int questionCount,

        @Schema(description = "누적 응답 수", example = "9")
        int totalResponseCount,

        @Schema(description = "활성 여부", example = "true")
        boolean isActive
) {
    public static SurveyListItemResponse of(Survey survey, int questionCount, int totalResponseCount) {
        return new SurveyListItemResponse(
                survey.getId(),
                survey.getTitle(),
                questionCount,
                totalResponseCount,
                survey.isActive()
        );
    }
}
