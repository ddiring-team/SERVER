package com.ddiring.ddiring_server.global.client.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record TransformQuestionsRequest(

        @JsonProperty("elder_profile")
        ElderProfile elderProfile,

        List<QuestionItem> questions,

        @JsonProperty("recent_responses")
        List<RecentResponse> recentResponses
) {
    public record ElderProfile(String name) {}

    public record QuestionItem(
            String key,
            String original,
            @JsonProperty("response_type") String responseType
    ) {}

    public record RecentResponse(
            String date,
            Map<String, String> responses
    ) {}
}
