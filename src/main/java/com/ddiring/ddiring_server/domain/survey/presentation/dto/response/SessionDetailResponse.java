package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "설문 세션 상세 (응답 내용 + AI 요약)")
public record SessionDetailResponse(

        @Schema(description = "세션 ID", example = "1")
        Long sessionId,

        @Schema(description = "설문 제목", example = "오늘의 안부 설문")
        String surveyTitle,

        @Schema(description = "설문 날짜", example = "2026-05-22")
        LocalDate sessionDate,

        @Schema(description = "완료 일시", example = "2026-05-22T18:46:00")
        LocalDateTime completedAt,

        @Schema(description = "카테고리별 답변 목록")
        List<AnswerItem> answers,

        @Schema(description = "AI 생성 일일 요약 (생성 전이면 null)")
        String summary,

        @Schema(description = "핵심 관찰 포인트 키워드 리스트 (생성 전이면 null)")
        List<String> highlights
) {
    @Schema(description = "개별 답변 항목")
    public record AnswerItem(

            @Schema(description = "카테고리", example = "기분")
            String category,

            @Schema(description = "질문 내용", example = "오늘 기분이 어떠세요?")
            String question,

            @Schema(description = "답변 텍스트", example = "그저그래요")
            String answer
    ) {}
}
