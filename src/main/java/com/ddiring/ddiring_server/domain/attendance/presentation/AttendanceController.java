package com.ddiring.ddiring_server.domain.attendance.presentation;

import com.ddiring.ddiring_server.domain.attendance.application.service.AttendanceService;
import com.ddiring.ddiring_server.domain.attendance.presentation.dto.response.AttendanceMonthlyResponse;
import com.ddiring.ddiring_server.domain.attendance.presentation.dto.response.AttendanceTodayResponse;
import com.ddiring.ddiring_server.domain.attendance.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "출석", description = "출석 관련 API")
@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(
            summary = "출석 체크 (어르신)",
            description = "어르신이 오늘의 출석을 체크합니다. 하루에 한 번만 가능하며, 출석 완료 시 같은 가족방의 보호자에게 FCM 푸시 알림이 발송됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "출석 체크 성공 (보호자에게 FCM 알림 발송)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "오늘 이미 출석 체크 완료",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping
    public ApiResponse<Void> checkIn(@AuthenticationPrincipal Long userId) {
        attendanceService.checkIn(userId);
        return ApiResponse.success(HttpStatus.CREATED, ResponseMessage.ATTENDANCE_CHECK_IN_SUCCESS.getMessage());
    }

    @Operation(summary = "내 오늘 출석 여부 조회 (어르신)", description = "어르신이 오늘 출석 체크를 완료했는지 확인합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceTodayResponse.class)))
    })
    @GetMapping("/me/today")
    public ApiResponse<AttendanceTodayResponse> getMyTodayAttendance(@AuthenticationPrincipal Long userId) {
        AttendanceTodayResponse response = attendanceService.getMyTodayAttendance(userId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.ATTENDANCE_TODAY_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "오늘 출석 여부 조회 (보호자)", description = "보호자가 같은 가족방 어르신의 오늘 출석 완료 여부를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceTodayResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방의 어르신이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방 멤버 정보 없음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/elders/{elderId}/today")
    public ApiResponse<AttendanceTodayResponse> getTodayAttendance(
            @Parameter(description = "어르신 사용자 ID") @PathVariable Long elderId,
            @AuthenticationPrincipal Long guardianId
    ) {
        AttendanceTodayResponse response = attendanceService.getTodayAttendance(guardianId, elderId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.ATTENDANCE_TODAY_SUCCESS.getMessage(), response);
    }

    @Operation(
            summary = "어르신에게 출석 독려 알림 발송 (보호자)",
            description = "오늘 아직 출석하지 않은 어르신에게 출석 독려 FCM 푸시를 발송합니다. " +
                    "어르신이 오늘 이미 출석했으면 409를 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "독려 알림 발송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방의 어르신이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "어르신을 찾을 수 없음",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "어르신이 오늘 이미 출석함",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/elders/{elderId}/reminder")
    public ApiResponse<Void> sendAttendanceReminder(
            @Parameter(description = "어르신 사용자 ID") @PathVariable Long elderId,
            @AuthenticationPrincipal Long guardianId
    ) {
        attendanceService.sendAttendanceReminder(guardianId, elderId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.ATTENDANCE_REMINDER_SUCCESS.getMessage(), null);
    }

    @Operation(summary = "월별 출석 현황 조회 (보호자)", description = "보호자가 같은 가족방 어르신의 월별 출석 현황을 조회합니다. 출석한 일(day) 목록과 총 횟수를 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceMonthlyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방의 어르신이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방 멤버 정보 없음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/elders/{elderId}/monthly")
    public ApiResponse<AttendanceMonthlyResponse> getMonthlyAttendance(
            @Parameter(description = "어르신 사용자 ID") @PathVariable Long elderId,
            @Parameter(description = "조회 연도 (예: 2026)") @RequestParam int year,
            @Parameter(description = "조회 월 (1~12)") @RequestParam int month,
            @AuthenticationPrincipal Long guardianId
    ) {
        AttendanceMonthlyResponse response = attendanceService.getMonthlyAttendance(guardianId, elderId, year, month);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.ATTENDANCE_MONTHLY_SUCCESS.getMessage(), response);
    }
}
