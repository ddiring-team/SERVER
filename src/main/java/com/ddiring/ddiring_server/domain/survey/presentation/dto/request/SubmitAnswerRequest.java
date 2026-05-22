package com.ddiring.ddiring_server.domain.survey.presentation.dto.request;

import java.util.List;

public record SubmitAnswerRequest(
        Long questionId,
        List<Long> selectedOptionIds, // YES_NO / SCALE / MULTIPLE 타입
        String answerText             // TEXT 타입
) {}
