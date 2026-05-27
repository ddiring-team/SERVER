package com.ddiring.ddiring_server.domain.survey.presentation;

import com.ddiring.ddiring_server.domain.survey.application.service.SurveyService;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.CreateSurveyRequest;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.CreateSurveyResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SurveyListItemResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.TodaySurveyResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "설문", description = "설문 관련 API")
@RestController
@RequestMapping("/api/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(
            summary = "설문 생성 (보호자)",
            description = "보호자가 기본 질문 선택 및 직접 질문 추가를 통해 새 설문을 생성합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "설문 생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateSurveyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "보호자가 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방에 소속되지 않음",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping
    public ApiResponse<CreateSurveyResponse> createSurvey(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateSurveyRequest request
    ) {
        CreateSurveyResponse response = surveyService.createSurvey(userId, request);
        return ApiResponse.success(HttpStatus.CREATED, ResponseMessage.SURVEY_CREATE_SUCCESS.getMessage(), response);
    }

    @Operation(
            summary = "오늘의 설문 조회 (어르신)",
            description = "어르신이 오늘 진행해야 할 활성 설문을 조회합니다. " +
                    "이미 오늘 완료한 설문은 제외되며, 진행할 설문이 없으면 data가 null로 반환됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (설문 없으면 data=null)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방에 소속되지 않음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/today")
    public ApiResponse<TodaySurveyResponse> getTodaySurvey(@AuthenticationPrincipal Long userId) {
        Optional<TodaySurveyResponse> result = surveyService.getTodaySurvey(userId);
        if (result.isPresent()) {
            return ApiResponse.success(HttpStatus.OK, ResponseMessage.TODAY_SURVEY_SUCCESS.getMessage(), result.get());
        }
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.TODAY_SURVEY_NONE.getMessage(), null);
    }

    @Operation(summary = "설문 목록 조회 (보호자)", description = "내 가족방의 설문 목록을 최신순으로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방에 소속되지 않음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping
    public ApiResponse<List<SurveyListItemResponse>> getSurveyList(@AuthenticationPrincipal Long userId) {
        List<SurveyListItemResponse> response = surveyService.getSurveyList(userId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.SURVEY_LIST_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "설문 활성화 (보호자)", description = "설문을 활성화하고 어르신에게 알림을 발송합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "활성화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 생성한 설문이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "설문 없음",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("/{surveyId}/activate")
    public ApiResponse<Void> activateSurvey(
            @Parameter(description = "설문 ID") @PathVariable Long surveyId,
            @AuthenticationPrincipal Long userId
    ) {
        surveyService.activateSurvey(userId, surveyId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.SURVEY_ACTIVATE_SUCCESS.getMessage());
    }

    @Operation(summary = "설문 삭제 (보호자)", description = "설문을 삭제합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 생성한 설문이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "설문 없음",
                    content = @Content(schema = @Schema()))
    })
    @DeleteMapping("/{surveyId}")
    public ApiResponse<Void> deleteSurvey(
            @Parameter(description = "설문 ID") @PathVariable Long surveyId,
            @AuthenticationPrincipal Long userId
    ) {
        surveyService.deleteSurvey(userId, surveyId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.SURVEY_DELETE_SUCCESS.getMessage());
    }
}
