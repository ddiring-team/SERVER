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
public class AttendanceMonitorScheduler {

    private static final int INACTIVE_DAYS = 3;
    private static final int COOLDOWN_HOURS = 24;

    private final UserRepository userRepository;
    private final AttendanceAlertProcessor processor;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void monitor() {
        LocalDate today = LocalDate.now();
        LocalDate windowStart = today.minusDays(INACTIVE_DAYS - 1); // 오늘 포함 최근 3일
        LocalDateTime cooldownSince = LocalDateTime.now().minusHours(COOLDOWN_HOURS);

        List<User> elders = userRepository.findAllByRole(Role.ELDER);
        log.info("출석 모니터링 시작 — 대상 어르신 {}명", elders.size());

        int alerted = 0;
        for (User elder : elders) {
            try {
                if (processor.processElder(elder, windowStart, today, cooldownSince, INACTIVE_DAYS)) {
                    alerted++;
                }
            } catch (Exception e) {
                log.warn("출석 미진행 알림 처리 실패: elderId={}, error={}", elder.getId(), e.toString());
            }
        }
        log.info("출석 모니터링 완료 — 알림 발송 {}건", alerted);
    }
}
