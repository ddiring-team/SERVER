package com.ddiring.ddiring_server.domain.distance.application.scheduler;

import com.ddiring.ddiring_server.domain.alert.domain.entity.RiskAlertHistory;
import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.AlertType;
import com.ddiring.ddiring_server.domain.alert.domain.repository.RiskAlertHistoryRepository;
import com.ddiring.ddiring_server.domain.distance.domain.entity.PairDistance;
import com.ddiring.ddiring_server.domain.distance.domain.repository.PairDistanceRepository;
import com.ddiring.ddiring_server.global.notification.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistanceScheduler {

    private static final String MAX_ALERT_TITLE = "안부거리 알림";

    private final PairDistanceRepository pairDistanceRepository;
    private final RiskAlertHistoryRepository riskAlertHistoryRepository;
    private final FcmService fcmService;

    /**
     * 매일 자정 5분에 모든 페어의 거리를 1km씩 증가시킨다 (상한 10km).
     * 당일 이미 액션이 있었던 페어는 lastActionAt이 오늘이므로 skip.
     * 10km에 도달한 페어는 보호자/어르신 양쪽에 FCM 알림 발송.
     */
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void incrementDistances() {
        List<PairDistance> all = pairDistanceRepository.findAll();
        LocalDate today = LocalDate.now();
        int incremented = 0;
        int maxReached = 0;

        for (PairDistance pd : all) {
            if (pd.getLastActionAt() != null && pd.getLastActionAt().toLocalDate().isEqual(today)) {
                continue;
            }
            if (pd.increment()) {
                incremented++;
            }

            if (pd.shouldNotifyMax()) {
                notifyMaxReached(pd);
                pd.markMaxNotified();
                maxReached++;
            }
        }

        log.info("안부거리 증가 완료 — 증가: {}, 10km 도달: {}", incremented, maxReached);
    }

    private void notifyMaxReached(PairDistance pd) {
        String elderName = pd.getElder().getName() != null ? pd.getElder().getName() : "어르신";
        String guardianName = pd.getGuardian().getName() != null ? pd.getGuardian().getName() : "보호자";

        String toGuardian = elderName + " 어르신과의 안부거리가 10km에 도달했어요. 오늘 한 번 안부를 전해보세요.";
        String toElder = guardianName + "님과의 안부거리가 10km에 도달했어요.";

        if (pd.getGuardian().getFcmToken() != null) {
            fcmService.sendToTokens(List.of(pd.getGuardian().getFcmToken()), MAX_ALERT_TITLE, toGuardian);
        }
        if (pd.getElder().getFcmToken() != null) {
            fcmService.sendToTokens(List.of(pd.getElder().getFcmToken()), MAX_ALERT_TITLE, toElder);
        }

        try {
            riskAlertHistoryRepository.save(RiskAlertHistory.builder()
                    .elder(pd.getElder())
                    .alertType(AlertType.DISTANCE_MAX_REACHED)
                    .message(toGuardian)
                    .build());
        } catch (Exception e) {
            log.warn("거리 알림 히스토리 저장 실패: pairId={}, error={}", pd.getId(), e.toString());
        }
    }
}
