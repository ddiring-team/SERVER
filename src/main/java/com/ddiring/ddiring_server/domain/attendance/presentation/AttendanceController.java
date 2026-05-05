package com.ddiring.ddiring_server.domain.attendance.presentation;

import com.ddiring.ddiring_server.domain.attendance.application.service.AttendanceService;
import com.ddiring.ddiring_server.domain.attendance.presentation.dto.response.AttendanceMonthlyResponse;
import com.ddiring.ddiring_server.domain.attendance.presentation.dto.response.AttendanceTodayResponse;
import com.ddiring.ddiring_server.domain.attendance.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ApiResponse<Void> checkIn(@AuthenticationPrincipal Long userId) {
        attendanceService.checkIn(userId);
        return ApiResponse.success(HttpStatus.CREATED, ResponseMessage.ATTENDANCE_CHECK_IN_SUCCESS.getMessage());
    }

    @GetMapping("/elders/{elderId}/today")
    public ApiResponse<AttendanceTodayResponse> getTodayAttendance(
            @PathVariable Long elderId,
            @AuthenticationPrincipal Long guardianId
    ) {
        AttendanceTodayResponse response = attendanceService.getTodayAttendance(guardianId, elderId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.ATTENDANCE_TODAY_SUCCESS.getMessage(), response);
    }

    @GetMapping("/elders/{elderId}/monthly")
    public ApiResponse<AttendanceMonthlyResponse> getMonthlyAttendance(
            @PathVariable Long elderId,
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal Long guardianId
    ) {
        AttendanceMonthlyResponse response = attendanceService.getMonthlyAttendance(guardianId, elderId, year, month);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.ATTENDANCE_MONTHLY_SUCCESS.getMessage(), response);
    }
}