package com.ddiring.ddiring_server.global.client.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record WeeklyReportRequest(

        @JsonProperty("elder_profile")
        ElderProfile elderProfile,

        @JsonProperty("start_date")
        String startDate,

        @JsonProperty("end_date")
        String endDate,

        @JsonProperty("weekly_responses")
        List<DailyResponse> weeklyResponses
) {
    public record ElderProfile(String name) {}

    public record DailyResponse(
            String date,
            Map<String, String> responses
    ) {}
}
