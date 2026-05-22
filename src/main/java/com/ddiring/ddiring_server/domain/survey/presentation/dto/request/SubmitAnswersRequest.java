package com.ddiring.ddiring_server.domain.survey.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitAnswersRequest(
        @NotEmpty @Valid List<SubmitAnswerRequest> answers
) {}
