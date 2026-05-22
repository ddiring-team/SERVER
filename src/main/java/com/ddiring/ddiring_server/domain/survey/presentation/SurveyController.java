package com.ddiring.ddiring_server.domain.survey.presentation;

import com.ddiring.ddiring_server.domain.survey.application.service.SurveyService;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.CreateSurveyRequest;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.CreateSurveyResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SurveyListItemResponse;
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

    @Operation(summary = "설문 활성/비활성 전환 (보호자)", description = "설문의 활성 상태를 토글합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인이 생성한 설문이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "설문 없음",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("/{surveyId}/status")
    public ApiResponse<Void> toggleSurveyStatus(
            @Parameter(description = "설문 ID") @PathVariable Long surveyId,
            @AuthenticationPrincipal Long userId
    ) {
        surveyService.toggleSurveyStatus(userId, surveyId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.SURVEY_STATUS_TOGGLE_SUCCESS.getMessage());
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
