package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import com.ddiring.ddiring_server.domain.survey.domain.entity.Survey;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "오늘의 설문 정보")
public record TodaySurveyResponse(

        @Schema(description = "설문 ID", example = "3")
        Long surveyId,

        @Schema(description = "설문 제목", example = "오늘의 안부 설문")
        String title
) {
    public static TodaySurveyResponse of(Survey survey) {
        return new TodaySurveyResponse(survey.getId(), survey.getTitle());
    }
}
