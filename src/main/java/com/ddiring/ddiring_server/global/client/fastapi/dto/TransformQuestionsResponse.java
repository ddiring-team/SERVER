package com.ddiring.ddiring_server.global.client.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TransformQuestionsResponse(

        @JsonProperty("transformed_questions")
        List<TransformedQuestion> transformedQuestions
) {
    public record TransformedQuestion(
            String key,
            String original,
            @JsonProperty("response_type") String responseType,
            String transformed,
            @JsonProperty("audio_url") String audioUrl
    ) {}
}
