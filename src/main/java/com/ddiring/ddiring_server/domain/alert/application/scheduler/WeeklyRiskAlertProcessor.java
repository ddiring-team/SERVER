package com.ddiring.ddiring_server.domain.alert.application.scheduler;

import com.ddiring.ddiring_server.domain.alert.domain.entity.RiskAlertHistory;
import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.AlertType;
import com.ddiring.ddiring_server.domain.alert.domain.repository.RiskAlertHistoryRepository;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.survey.application.service.WeeklyReportService;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.WeeklyReportResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse.Pattern;
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
public class WeeklyRiskAlertProcessor {

    private static final String ALERT_TITLE = "주간 건강 주의 알림";
    private static final String SEVERITY_WARNING = "warning";  // info는 알림 제외

    private final WeeklyReportService weeklyReportService;
    private final FamilyMemberRepository familyMemberRepository;
    private final RiskAlertHistoryRepository riskAlertHistoryRepository;
    private final FcmService fcmService;

    /**
     * 어르신 1명에 대한 주간 위험 알림 처리. 각자 독립된 트랜잭션으로 격리.
     * @return 발송된 알림 수
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int processElder(User elder, LocalDate startDate, LocalDate endDate, LocalDateTime cooldownSince) {
        WeeklyReportResponse report = weeklyReportService.generateForElder(elder.getId(), startDate, endDate);
        if (report == null || report.patterns() == null) return 0;

        int alerted = 0;
        for (Pattern pattern : report.patterns()) {
            if (!isRiskSeverity(pattern.severity())) continue;
            if (pattern.category() == null) continue;

            if (riskAlertHistoryRepository.existsRecentByElderAndTypeAndCategory(
                    elder.getId(), AlertType.WEEKLY_RISK, pattern.category(), cooldownSince)) {
                continue;
            }

            if (sendAlert(elder, pattern)) {
                alerted++;
            }
        }
        return alerted;
    }

    private boolean isRiskSeverity(String severity) {
        return severity != null && SEVERITY_WARNING.equalsIgnoreCase(severity);
    }

    private boolean sendAlert(User elder, Pattern pattern) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(elder.getId()).orElse(null);
        if (familyId == null) return false;

        List<String> tokens = familyMemberRepository.findGuardianFcmTokensByFamilyId(familyId);
        String elderName = elder.getName() != null ? elder.getName() : "어르신";
        String message = elderName + " 어르신의 지난 주 [" + pattern.category() + "] 항목에서 주의가 필요해요: "
                + pattern.observation();

        fcmService.sendToTokens(tokens, ALERT_TITLE, message);

        riskAlertHistoryRepository.save(
                RiskAlertHistory.builder()
                        .elder(elder)
                        .alertType(AlertType.WEEKLY_RISK)
                        .category(pattern.category())
                        .message(truncate(message, 500))
                        .build()
        );
        return true;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
