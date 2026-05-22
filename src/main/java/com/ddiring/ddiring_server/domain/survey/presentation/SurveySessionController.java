package com.ddiring.ddiring_server.domain.survey.presentation;

import com.ddiring.ddiring_server.domain.survey.application.service.SurveySessionService;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.StartSurveySessionRequest;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.StartSurveySessionResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "설문 세션", description = "어르신의 설문 응답 세션 API")
@RestController
@RequestMapping("/api/survey-sessions")
@RequiredArgsConstructor
public class SurveySessionController {

    private final SurveySessionService surveySessionService;

    @Operation(
            summary = "설문 세션 시작 (어르신)",
            description = "어르신이 활성 설문을 시작합니다. 같은 날 시작한 세션이 있으면 이어서 진행합니다. " +
                    "FastAPI가 어르신 이름·최근 응답을 반영해 질문을 개인화된 발화로 변환하며, " +
                    "변환에 실패하면 원본 질문 내용으로 fallback 합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "세션 시작 성공",
                    content = @Content(schema = @Schema(implementation = StartSurveySessionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "어르신이 아니거나 다른 가족방 설문",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "설문 또는 가족방 정보 없음",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "비활성 설문 또는 오늘 이미 응답 완료",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping
    public ApiResponse<StartSurveySessionResponse> startSession(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody StartSurveySessionRequest request
    ) {
        StartSurveySessionResponse response = surveySessionService.startSession(userId, request.surveyId());
        return ApiResponse.success(HttpStatus.CREATED, ResponseMessage.SURVEY_SESSION_START_SUCCESS.getMessage(), response);
    }
}
