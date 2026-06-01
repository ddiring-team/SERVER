package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "어르신의 오늘 설문 완료 여부")
public record ElderTodaySurveyResponse(

        @Schema(description = "오늘 설문을 완료했는지 여부", example = "true")
        boolean completed
) {
    public static ElderTodaySurveyResponse of(boolean completed) {
        return new ElderTodaySurveyResponse(completed);
    }
}
