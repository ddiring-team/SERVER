package com.ddiring.ddiring_server.domain.survey.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "설문 세션 시작 요청")
public record StartSurveySessionRequest(

        @Schema(description = "응답할 설문 ID", example = "1")
        @NotNull
        Long surveyId
) {}
