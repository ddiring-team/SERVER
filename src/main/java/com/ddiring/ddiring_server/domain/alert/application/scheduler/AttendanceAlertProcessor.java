package com.ddiring.ddiring_server.domain.alert.application.scheduler;

import com.ddiring.ddiring_server.domain.alert.domain.entity.RiskAlertHistory;
import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.AlertType;
import com.ddiring.ddiring_server.domain.alert.domain.repository.RiskAlertHistoryRepository;
import com.ddiring.ddiring_server.domain.attendance.domain.repository.AttendanceRepository;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.notification.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AttendanceAlertProcessor {

    private static final String ALERT_TITLE = "출석 미진행 알림";

    private final AttendanceRepository attendanceRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final RiskAlertHistoryRepository riskAlertHistoryRepository;
    private final FcmService fcmService;

    /**
     * 어르신 1명에 대한 출석 미진행 알림 처리. 각자 독립된 트랜잭션으로 격리.
     * @return 알림이 발송되었으면 true
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean processElder(User elder, LocalDate windowStart, LocalDate today,
                                LocalDateTime cooldownSince, int inactiveDays) {
        long count = attendanceRepository.countByUserIdAndDateBetween(elder.getId(), windowStart, today);
        if (count > 0) return false;

        if (riskAlertHistoryRepository.existsRecentByElderAndType(
                elder.getId(), AlertType.ATTENDANCE_INACTIVE, cooldownSince)) {
            return false;
        }

        Long familyId = familyMemberRepository.findFamilyIdByUserId(elder.getId()).orElse(null);
        if (familyId == null) return false;

        List<String> tokens = familyMemberRepository.findGuardianFcmTokensByFamilyId(familyId);
        String elderName = elder.getName() != null ? elder.getName() : "어르신";
        String message = elderName + " 어르신께서 " + inactiveDays + "일째 출석을 하지 않으셨어요. 한 번 확인 부탁드려요.";

        fcmService.sendToTokens(tokens, ALERT_TITLE, message);

        riskAlertHistoryRepository.save(
                RiskAlertHistory.builder()
                        .elder(elder)
                        .alertType(AlertType.ATTENDANCE_INACTIVE)
                        .message(message)
                        .build()
        );
        return true;
    }
}
