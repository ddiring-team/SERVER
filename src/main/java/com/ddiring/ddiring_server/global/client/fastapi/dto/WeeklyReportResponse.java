package com.ddiring.ddiring_server.global.client.fastapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "FastAPI 주간 패턴 리포트 응답")
public record WeeklyReportResponse(

        @Schema(description = "AI가 생성한 주간 리포트 본문 텍스트",
                example = "이번 주 김순자 어르신은 전반적으로 안정적이었으나, 주중 수면의 질이 다소 떨어진 모습이 관찰되었습니다.")
        String report,

        @Schema(description = "분석된 패턴(주의 항목 등) 목록")
        List<Pattern> patterns,

        @Schema(description = "주간 응답 통계 정보")
        Stats stats
) {
    @Schema(description = "AI가 감지한 개별 패턴")
    public record Pattern(

            @Schema(description = "패턴이 속한 설문 카테고리", example = "건강 상태")
            String category,

            @Schema(description = "관찰된 내용 설명", example = "주 3일 이상 '잠을 설쳤다'고 응답")
            String observation,

            @Schema(description = "심각도 수준 (예: info / warning / danger)", example = "warning")
            String severity,

            @Schema(description = "패턴의 근거가 된 응답 날짜 목록 (YYYY-MM-DD)",
                    example = "[\"2026-05-15\", \"2026-05-17\", \"2026-05-19\"]")
            @JsonProperty("evidence_dates")
            List<String> evidenceDates
    ) {}

    @Schema(description = "주간 응답 통계")
    public record Stats(

            @Schema(description = "해당 주에 분석된 설문 카테고리 목록",
                    example = "[\"식사 / 수분\", \"건강 상태\", \"기분 / 감정\", \"활동 / 외출\"]")
            List<String> categories,

            @Schema(description = "리포트 대상 기간 및 응답 일수 정보")
            @JsonProperty("date_range")
            DateRange dateRange,

            @Schema(description = "카테고리별 세부 통계 (키: 카테고리명, 값: timeline/value_counts 객체)",
                    example = "{\"식사 / 수분\": {\"value_counts\": {\"예\": 5, \"아니요\": 2}}, "
                            + "\"건강 상태\": {\"value_counts\": {\"괜찮아요\": 4, \"조금 불편\": 3}}}")
            @JsonProperty("by_category")
            Map<String, Object> byCategory
    ) {}

    @Schema(description = "리포트 대상 기간 및 응답 일수")
    public record DateRange(

            @Schema(description = "기간 시작일 (YYYY-MM-DD)", example = "2026-05-14")
            String start,

            @Schema(description = "기간 종료일 (YYYY-MM-DD)", example = "2026-05-20")
            String end,

            @Schema(description = "기간 내 전체 일수", example = "7")
            @JsonProperty("total_days")
            int totalDays,

            @Schema(description = "응답이 존재한 일수", example = "6")
            @JsonProperty("days_with_response")
            int daysWithResponse
    ) {}
}
