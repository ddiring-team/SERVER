package com.ddiring.ddiring_server.domain.alert.application.scheduler;

import com.ddiring.ddiring_server.domain.alert.domain.entity.RiskAlertHistory;
import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.AlertType;
import com.ddiring.ddiring_server.domain.alert.domain.repository.RiskAlertHistoryRepository;
import com.ddiring.ddiring_server.domain.attendance.domain.repository.AttendanceRepository;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.global.notification.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceMonitorScheduler {

    private static final int INACTIVE_DAYS = 3;
    private static final int COOLDOWN_HOURS = 24;
    private static final String ALERT_TITLE = "출석 미진행 알림";

    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final RiskAlertHistoryRepository riskAlertHistoryRepository;
    private final FcmService fcmService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    @Transactional
    public void monitor() {
        LocalDate today = LocalDate.now();
        LocalDate windowStart = today.minusDays(INACTIVE_DAYS - 1); // 오늘 포함 최근 3일
        LocalDateTime cooldownSince = LocalDateTime.now().minusHours(COOLDOWN_HOURS);

        List<User> elders = userRepository.findAllByRole(Role.ELDER);
        log.info("출석 모니터링 시작 — 대상 어르신 {}명", elders.size());

        int alerted = 0;
        for (User elder : elders) {
            long count = attendanceRepository.countByUserIdAndDateBetween(elder.getId(), windowStart, today);
            if (count > 0) continue;

            if (riskAlertHistoryRepository.existsRecentByElderAndType(
                    elder.getId(), AlertType.ATTENDANCE_INACTIVE, cooldownSince)) {
                continue;
            }

            sendAlert(elder);
            alerted++;
        }
        log.info("출석 모니터링 완료 — 알림 발송 {}건", alerted);
    }

    private void sendAlert(User elder) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(elder.getId()).orElse(null);
        if (familyId == null) return;

        List<String> tokens = familyMemberRepository.findGuardianFcmTokensByFamilyId(familyId);
        String elderName = elder.getName() != null ? elder.getName() : "어르신";
        String message = elderName + " 어르신께서 " + INACTIVE_DAYS + "일째 출석을 하지 않으셨어요. 한 번 확인 부탁드려요.";

        fcmService.sendToTokens(tokens, ALERT_TITLE, message);

        riskAlertHistoryRepository.save(
                RiskAlertHistory.builder()
                        .elder(elder)
                        .alertType(AlertType.ATTENDANCE_INACTIVE)
                        .message(message)
                        .build()
        );
    }
}
