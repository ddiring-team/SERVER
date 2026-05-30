package com.ddiring.ddiring_server.domain.attendance.application.service;

import com.ddiring.ddiring_server.domain.attendance.domain.entity.Attendance;
import com.ddiring.ddiring_server.domain.attendance.domain.repository.AttendanceRepository;
import com.ddiring.ddiring_server.domain.attendance.exception.AlreadyAttendedException;
import com.ddiring.ddiring_server.domain.attendance.presentation.dto.response.AttendanceMonthlyResponse;
import com.ddiring.ddiring_server.domain.attendance.presentation.dto.response.AttendanceTodayResponse;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.family.exception.ElderNotInFamilyException;
import com.ddiring.ddiring_server.domain.family.exception.FamilyMemberNotFoundException;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import com.ddiring.ddiring_server.domain.distance.application.event.DistanceResetEvent;
import com.ddiring.ddiring_server.domain.distance.domain.entity.enums.DistanceActionType;
import com.ddiring.ddiring_server.global.notification.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void checkIn(Long userId) {
        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByUser_IdAndCheckedAt(userId, today)) {
            throw new AlreadyAttendedException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        attendanceRepository.save(
                Attendance.builder()
                        .user(user)
                        .checkedAt(today)
                        .build()
        );

        familyMemberRepository.findFamilyIdByUserId(userId)
                .ifPresent(familyId -> {
            List<String> guardianTokens = familyMemberRepository.findGuardianFcmTokensByFamilyId(familyId);
            String elderName = user.getName() != null ? user.getName() : "어르신";
            fcmService.sendToTokens(guardianTokens, "출석 완료 알림", elderName + "님이 오늘 출석을 완료했어요!");
        });

        eventPublisher.publishEvent(DistanceResetEvent.broadcast(userId, DistanceActionType.ATTENDANCE));
    }

    @Transactional(readOnly = true)
    public AttendanceTodayResponse getTodayAttendance(Long guardianId, Long elderId) {
        validateSameFamily(guardianId, elderId);

        boolean attended = attendanceRepository.existsByUser_IdAndCheckedAt(elderId, LocalDate.now());
        return AttendanceTodayResponse.of(attended);
    }

    @Transactional(readOnly = true)
    public AttendanceMonthlyResponse getMonthlyAttendance(Long guardianId, Long elderId, int year, int month) {
        validateSameFamily(guardianId, elderId);

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Integer> attendedDays = attendanceRepository
                .findCheckedDatesByUserIdAndDateBetween(elderId, start, end)
                .stream()
                .map(LocalDate::getDayOfMonth)
                .sorted()
                .toList();

        return AttendanceMonthlyResponse.of(year, month, attendedDays);
    }

    @Transactional(readOnly = true)
    public AttendanceTodayResponse getMyTodayAttendance(Long userId) {
        boolean attended = attendanceRepository.existsByUser_IdAndCheckedAt(userId, LocalDate.now());
        return AttendanceTodayResponse.of(attended);
    }

    private void validateSameFamily(Long guardianId, Long elderId) {
        Long guardianFamilyId = familyMemberRepository.findFamilyIdByUserId(guardianId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        Long elderFamilyId = familyMemberRepository.findFamilyIdByUserId(elderId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        if (!guardianFamilyId.equals(elderFamilyId)) {
            throw new ElderNotInFamilyException();
        }
    }
}