package com.ddiring.ddiring_server.global.client.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record DailySummaryRequest(

        @JsonProperty("elder_profile")
        ElderProfile elderProfile,

        @JsonProperty("daily_response")
        DailyResponse dailyResponse
) {
    public record ElderProfile(String name) {}

    public record DailyResponse(
            String date,
            Map<String, String> responses
    ) {}
}
