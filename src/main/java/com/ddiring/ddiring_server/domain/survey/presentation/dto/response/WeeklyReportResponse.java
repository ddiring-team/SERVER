package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse.Pattern;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse.Stats;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "주간 패턴 리포트")
public record WeeklyReportResponse(

        @Schema(description = "어르신 ID")
        Long elderId,

        @Schema(description = "어르신 이름", example = "김순자")
        String elderName,

        @Schema(description = "리포트 시작 날짜", example = "2026-05-14")
        LocalDate startDate,

        @Schema(description = "리포트 종료 날짜", example = "2026-05-20")
        LocalDate endDate,

        @Schema(description = "AI 생성 주간 리포트 텍스트")
        String report,

        @Schema(description = "패턴 목록 (주의 항목 등)")
        List<Pattern> patterns,

        @Schema(description = "통계 정보")
        Stats stats
) {}
