package com.ddiring.ddiring_server.domain.temperature.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "지난 주(월~일) 일별 안부 온도 변화")
public record WeeklyTemperatureResponse(

        @Schema(description = "조회 시작일 (지난 주 월요일)", example = "2026-05-25")
        LocalDate startDate,

        @Schema(description = "조회 종료일 (지난 주 일요일)", example = "2026-05-31")
        LocalDate endDate,

        @Schema(description = "월~일 7일간의 일별 온도")
        List<DailyTemperatureItem> days
) {
    @Schema(description = "하루 단위 온도")
    public record DailyTemperatureItem(

            @Schema(description = "날짜", example = "2026-05-25")
            LocalDate date,

            @Schema(description = "요일", example = "MONDAY")
            DayOfWeek dayOfWeek,

            @Schema(description = "해당 날짜의 안부 온도 (기록이 없으면 null)", example = "38.5")
            BigDecimal temperature
    ) {
    }
}
