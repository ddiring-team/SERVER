package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.SurveyCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리별 주간 상태 요약 (백엔드 산출, 클라이언트 표시 전용)")
public record CategorySummary(

        @Schema(description = "카테고리 enum 코드 (아이콘/매핑 키)", example = "MEAL")
        String category,

        @Schema(description = "카테고리 표시명", example = "식사 / 수분")
        String displayName,

        @Schema(description = "상태 코드 (GOOD / INFO / ATTENTION / INSUFFICIENT / NO_DATA)", example = "ATTENTION")
        String status,

        @Schema(description = "상태 표시 라벨", example = "주의 필요")
        String statusLabel,

        @Schema(description = "우려 패턴 관찰 메모 (패턴 없으면 null)",
                example = "주중 식사를 거르신 날이 2회 관찰됨")
        String note,

        @Schema(description = "패턴 심각도 (info / warning, 패턴 없으면 null)", example = "warning")
        String severity,

        @Schema(description = "해당 카테고리 응답 일수", example = "7")
        int responseDays
) {
    /** 해당 주 응답이 없는 카테고리의 빈 카드. */
    public static CategorySummary noData(SurveyCategory category) {
        return new CategorySummary(
                category.name(),
                category.getDisplayName(),
                CategoryStatus.NO_DATA.name(),
                CategoryStatus.NO_DATA.getLabel(),
                null,
                null,
                0
        );
    }
}
