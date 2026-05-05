package com.ddiring.ddiring_server.domain.attendance.presentation.dto.response;

import java.util.List;

public record AttendanceMonthlyResponse(
        int year,
        int month,
        List<Integer> attendedDays,
        int totalCount
) {
    public static AttendanceMonthlyResponse of(int year, int month, List<Integer> attendedDays) {
        return new AttendanceMonthlyResponse(year, month, attendedDays, attendedDays.size());
    }
}