package com.ddiring.ddiring_server.domain.survey.presentation.dto.request;

public record SubmitAnswerRequest(
        Long questionId,
        Long selectedOptionId,  // YES_NO / SCALE / MULTIPLE 타입
        String answerText       // TEXT 타입
) {}
