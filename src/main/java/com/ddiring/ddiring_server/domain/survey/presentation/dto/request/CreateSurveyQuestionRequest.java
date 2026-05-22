package com.ddiring.ddiring_server.domain.survey.presentation.dto.request;

import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "설문 질문 생성 요청")
public record CreateSurveyQuestionRequest(

        @Schema(description = "질문 카테고리 (기본 질문 선택 시 제공, 직접 추가 시 null)", example = "식사/수분")
        @Size(max = 50)
        String category,

        @Schema(description = "질문 내용", example = "오늘 세 끼를 모두 드셨나요?")
        @NotBlank @Size(max = 200)
        String content,

        @Schema(description = "질문 유형 (YES_NO, MULTIPLE, TEXT)", example = "YES_NO")
        @NotNull
        QuestionType questionType,

        @Schema(description = "질문 순서", example = "1")
        @NotNull @Positive
        Integer orderNum,

        @Schema(description = "선택지 목록 (SCALE/MULTIPLE 타입 필수, 2~3개). YES_NO는 서버가 자동 생성, TEXT는 사용 안 함",
                example = "[\"좋아요\", \"보통\", \"별로\"]")
        @Size(max = 3)
        List<@NotBlank @Size(max = 50) String> options
) {}
