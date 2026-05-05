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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @DisplayName("오늘 출석한 이력이 없으면 출석 체크에 성공한다")
    @Test
    void checkIn_성공() {
        // given
        Long userId = 1L;
        User user = User.builder().build();

        given(attendanceRepository.existsByUser_IdAndCheckedAt(userId, LocalDate.now())).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        attendanceService.checkIn(userId);

        // then
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @DisplayName("오늘 이미 출석한 경우 AlreadyAttendedException 이 발생한다")
    @Test
    void checkIn_실패_중복출석() {
        // given
        Long userId = 1L;

        given(attendanceRepository.existsByUser_IdAndCheckedAt(userId, LocalDate.now())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(userId))
                .isInstanceOf(AlreadyAttendedException.class);
    }

    @DisplayName("같은 가족방의 어르신이 오늘 출석했으면 attended=true 를 반환한다")
    @Test
    void getTodayAttendance_성공_출석완료() {
        // given
        Long guardianId = 1L;
        Long elderId = 2L;

        given(familyMemberRepository.findFamilyIdByUserId(guardianId)).willReturn(Optional.of(10L));
        given(familyMemberRepository.findFamilyIdByUserId(elderId)).willReturn(Optional.of(10L));
        given(attendanceRepository.existsByUser_IdAndCheckedAt(elderId, LocalDate.now())).willReturn(true);

        // when
        AttendanceTodayResponse response = attendanceService.getTodayAttendance(guardianId, elderId);

        // then
        assertThat(response.attended()).isTrue();
    }

    @DisplayName("같은 가족방의 어르신이 오늘 출석하지 않았으면 attended=false 를 반환한다")
    @Test
    void getTodayAttendance_성공_미출석() {
        // given
        Long guardianId = 1L;
        Long elderId = 2L;

        given(familyMemberRepository.findFamilyIdByUserId(guardianId)).willReturn(Optional.of(10L));
        given(familyMemberRepository.findFamilyIdByUserId(elderId)).willReturn(Optional.of(10L));
        given(attendanceRepository.existsByUser_IdAndCheckedAt(elderId, LocalDate.now())).willReturn(false);

        // when
        AttendanceTodayResponse response = attendanceService.getTodayAttendance(guardianId, elderId);

        // then
        assertThat(response.attended()).isFalse();
    }

    @DisplayName("다른 가족방의 어르신을 조회하면 ElderNotInFamilyException 이 발생한다")
    @Test
    void getTodayAttendance_실패_다른가족방() {
        // given
        Long guardianId = 1L;
        Long elderId = 2L;

        given(familyMemberRepository.findFamilyIdByUserId(guardianId)).willReturn(Optional.of(10L));
        given(familyMemberRepository.findFamilyIdByUserId(elderId)).willReturn(Optional.of(99L));

        // when & then
        assertThatThrownBy(() -> attendanceService.getTodayAttendance(guardianId, elderId))
                .isInstanceOf(ElderNotInFamilyException.class);
    }

    @DisplayName("가족방에 속하지 않은 보호자가 조회하면 FamilyMemberNotFoundException 이 발생한다")
    @Test
    void getTodayAttendance_실패_가족방멤버아님() {
        // given
        Long guardianId = 1L;
        Long elderId = 2L;

        given(familyMemberRepository.findFamilyIdByUserId(guardianId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.getTodayAttendance(guardianId, elderId))
                .isInstanceOf(FamilyMemberNotFoundException.class);
    }

    @DisplayName("월별 출석 현황 조회 시 출석한 일(day) 목록과 총 횟수를 반환한다")
    @Test
    void getMonthlyAttendance_성공() {
        // given
        Long guardianId = 1L;
        Long elderId = 2L;
        int year = 2026;
        int month = 5;

        List<LocalDate> checkedDates = List.of(
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 10)
        );

        given(familyMemberRepository.findFamilyIdByUserId(guardianId)).willReturn(Optional.of(10L));
        given(familyMemberRepository.findFamilyIdByUserId(elderId)).willReturn(Optional.of(10L));
        given(attendanceRepository.findCheckedDatesByUserIdAndDateBetween(
                elderId,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        )).willReturn(checkedDates);

        // when
        AttendanceMonthlyResponse response = attendanceService.getMonthlyAttendance(guardianId, elderId, year, month);

        // then
        assertThat(response.year()).isEqualTo(2026);
        assertThat(response.month()).isEqualTo(5);
        assertThat(response.attendedDays()).containsExactly(1, 5, 10);
        assertThat(response.totalCount()).isEqualTo(3);
    }
}
