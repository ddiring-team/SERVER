package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "세션 시작 시 어르신에게 노출되는 질문")
public record SurveyQuestionForSessionResponse(

        @Schema(description = "질문 ID", example = "10")
        Long questionId,

        @Schema(description = "질문 순서", example = "1")
        int orderNum,

        @Schema(description = "질문 카테고리", example = "식사/수분")
        String category,

        @Schema(description = "질문 유형", example = "YES_NO")
        QuestionType questionType,

        @Schema(description = "원본 질문 내용", example = "오늘 세 끼를 모두 드셨나요?")
        String originalContent,

        @Schema(description = "AI 변환된 질문 발화. 변환 실패 시 originalContent와 동일",
                example = "김순자 어르신, 오늘 아침은 잘 챙겨 드셨어요?")
        String displayContent,

        @Schema(description = "질문 음성 URL", example = "https://example.com/audio/question.mp3")
        String audioUrl,

        @Schema(description = "선택지 목록 (TEXT 타입은 빈 배열)")
        List<SurveyOptionResponse> options
) {}
