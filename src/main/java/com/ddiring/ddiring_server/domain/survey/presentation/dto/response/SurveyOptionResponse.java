package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestionOption;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선택지")
public record SurveyOptionResponse(

        @Schema(description = "선택지 ID", example = "1")
        Long optionId,

        @Schema(description = "선택지 라벨", example = "예")
        String label,

        @Schema(description = "선택지 순서", example = "1")
        int orderNum
) {
    public static SurveyOptionResponse from(SurveyQuestionOption option) {
        return new SurveyOptionResponse(option.getId(), option.getLabel(), option.getOrderNum());
    }
}
