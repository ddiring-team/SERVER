package com.ddiring.ddiring_server.domain.alert.application.scheduler;

import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyRiskAlertScheduler {

    private static final int COOLDOWN_DAYS = 7;

    private final UserRepository userRepository;
    private final WeeklyRiskAlertProcessor processor;

    @Scheduled(cron = "0 0 20 ? * SUN", zone = "Asia/Seoul")
    public void monitor() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);  // 지난 7일 (오늘 포함)
        LocalDateTime cooldownSince = LocalDateTime.now().minusDays(COOLDOWN_DAYS);

        List<User> elders = userRepository.findAllByRole(Role.ELDER);
        log.info("주간 위험 알림 시작 — 대상 어르신 {}명, 기간 {}~{}", elders.size(), startDate, today);

        int alerted = 0;
        for (User elder : elders) {
            try {
                alerted += processor.processElder(elder, startDate, today, cooldownSince);
            } catch (Exception e) {
                log.warn("주간 위험 알림 처리 실패: elderId={}, error={}", elder.getId(), e.toString());
            }
        }
        log.info("주간 위험 알림 완료 — 발송 {}건", alerted);
    }
}
