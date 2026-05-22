package com.ddiring.ddiring_server.domain.survey.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "설문 생성 요청")
public record CreateSurveyRequest(

        @Schema(description = "설문 제목", example = "오늘의 안부 설문3")
        @NotBlank @Size(max = 100)
        String title,

        @Schema(description = "설문 질문 목록 (최소 1개)")
        @NotEmpty @Valid
        List<CreateSurveyQuestionRequest> questions
) {}
