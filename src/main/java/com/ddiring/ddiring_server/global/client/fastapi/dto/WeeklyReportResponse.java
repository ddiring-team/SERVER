package com.ddiring.ddiring_server.global.client.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record WeeklyReportResponse(
        String report,
        List<Pattern> patterns,
        Stats stats
) {
    public record Pattern(
            String category,
            String observation,
            String severity,

            @JsonProperty("evidence_dates")
            List<String> evidenceDates
    ) {}

    public record Stats(
            List<String> categories,

            @JsonProperty("date_range")
            DateRange dateRange,

            @JsonProperty("by_category")
            Map<String, Object> byCategory
    ) {}

    public record DateRange(
            String start,
            String end,

            @JsonProperty("total_days")
            int totalDays,

            @JsonProperty("days_with_response")
            int daysWithResponse
    ) {}
}
