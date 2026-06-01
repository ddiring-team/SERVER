package com.ddiring.ddiring_server.domain.survey.presentation;

import com.ddiring.ddiring_server.domain.survey.application.service.SurveyAnswerService;
import com.ddiring.ddiring_server.domain.survey.application.service.SurveySessionService;
import com.ddiring.ddiring_server.domain.survey.application.service.WeeklyReportService;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.StartSurveySessionRequest;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.SubmitAnswersRequest;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.ElderSessionListItemResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.ElderTodaySurveyResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SessionDetailResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.StartSurveySessionResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.WeeklyReportResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "설문 세션", description = "어르신의 설문 응답 세션 API")
@RestController
@RequestMapping("/api/survey-sessions")
@RequiredArgsConstructor
public class SurveySessionController {

    private final SurveySessionService surveySessionService;
    private final SurveyAnswerService surveyAnswerService;
    private final WeeklyReportService weeklyReportService;

    @Operation(
            summary = "어르신 주간 패턴 리포트 조회 (보호자)",
            description = "특정 어르신의 지정 기간 설문 응답을 분석한 AI 주간 리포트를 반환합니다. 같은 가족방 구성원만 조회 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방이 아님",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/weekly-report")
    public ApiResponse<WeeklyReportResponse> getWeeklyReport(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long elderId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        WeeklyReportResponse result = weeklyReportService.getWeeklyReport(userId, elderId, startDate, endDate);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.WEEKLY_REPORT_SUCCESS.getMessage(), result);
    }

    @Operation(
            summary = "어르신 설문 응답 목록 조회 (보호자)",
            description = "특정 어르신의 완료된 설문 세션 목록을 최신순으로 조회합니다. 같은 가족방 구성원만 조회 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방이 아님",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping
    public ApiResponse<List<ElderSessionListItemResponse>> getElderSessionList(
            @AuthenticationPrincipal Long userId,
            @RequestParam Long elderId
    ) {
        List<ElderSessionListItemResponse> result = surveySessionService.getElderSessionList(userId, elderId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.SESSION_LIST_SUCCESS.getMessage(), result);
    }

    @Operation(
            summary = "어르신 오늘 설문 완료 여부 조회 (보호자)",
            description = "보호자가 같은 가족방 어르신이 오늘 설문을 완료했는지 조회합니다. 같은 가족방 구성원만 조회 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ElderTodaySurveyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방이 아님",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/elders/{elderId}/today")
    public ApiResponse<ElderTodaySurveyResponse> getElderTodaySurveyStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long elderId
    ) {
        ElderTodaySurveyResponse result = surveySessionService.getElderTodaySurveyStatus(userId, elderId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.ELDER_TODAY_SURVEY_SUCCESS.getMessage(), result);
    }

    @Operation(
            summary = "어르신에게 설문 독려 알림 발송 (보호자)",
            description = "오늘 아직 설문을 완료하지 않은 어르신에게 설문 독려 FCM 푸시를 발송합니다. " +
                    "어르신이 오늘 이미 설문을 완료했으면 409를 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "독려 알림 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "어르신을 찾을 수 없음",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "어르신이 오늘 이미 설문을 완료함",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/elders/{elderId}/reminder")
    public ApiResponse<Void> sendSurveyReminder(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long elderId
    ) {
        surveySessionService.sendSurveyReminder(userId, elderId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.SURVEY_REMINDER_SUCCESS.getMessage(), null);
    }

    @Operation(
            summary = "설문 세션 상세 조회 (보호자/어르신)",
            description = "특정 세션의 응답 내용과 AI 요약을 조회합니다. AI 요약 생성 전이면 summary/highlights는 null로 반환됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/{sessionId}")
    public ApiResponse<SessionDetailResponse> getSessionDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId
    ) {
        SessionDetailResponse result = surveySessionService.getSessionDetail(userId, sessionId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.SESSION_DETAIL_SUCCESS.getMessage(), result);
    }

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

    @Operation(
            summary = "설문 답변 제출 (어르신)",
            description = "어르신이 세션의 모든 질문에 대한 답변을 한 번에 제출합니다. 제출 후 세션은 COMPLETED 상태로 변경됩니다. " +
                    "YES_NO/SCALE/MULTIPLE 타입은 selectedOptionId, TEXT 타입은 answerText를 입력합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "답변 제출 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "질문 유형에 맞지 않는 답변 형식",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인의 세션이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 완료된 세션",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/{sessionId}/answers")
    public ApiResponse<Void> submitAnswers(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswersRequest request
    ) {
        surveyAnswerService.submitAnswers(userId, sessionId, request.answers());
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.SURVEY_ANSWER_SUBMIT_SUCCESS.getMessage(), null);
    }
}
