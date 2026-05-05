package com.ddiring.ddiring_server.domain.attendance.presentation.dto.response;

public record AttendanceTodayResponse(
        boolean attended
) {
    public static AttendanceTodayResponse of(boolean attended) {
        return new AttendanceTodayResponse(attended);
    }
}