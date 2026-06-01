package com.ddiring.ddiring_server.domain.temperature.application.scheduler;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature;
import com.ddiring.ddiring_server.domain.temperature.domain.repository.DailyTemperatureRepository;
import com.ddiring.ddiring_server.domain.temperature.domain.repository.UserTemperatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyTemperatureSnapshotScheduler {

    private final UserTemperatureRepository userTemperatureRepository;
    private final DailyTemperatureRepository dailyTemperatureRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * 매일 자정 직전(23:55)에 모든 사용자의 그 시점 누적 온도를 오늘 날짜로 스냅샷한다.
     * <p>주간 온도 변화 조회는 이 일별 스냅샷을 집계해 제공한다.
     * UPSERT로 적재하므로 재실행되어도 중복 없이 멱등하다.
     * <p>사용자별 스냅샷은 {@code REQUIRES_NEW} 독립 트랜잭션으로 실행해, 한 사용자의
     * 적재 실패가 다른 사용자의 스냅샷까지 롤백시키지 않도록 격리한다.
     */
    @Scheduled(cron = "0 55 23 * * *", zone = "Asia/Seoul")
    public void snapshot() {
        LocalDate today = LocalDate.now();
        List<UserTemperature> all = userTemperatureRepository.findAll();

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (UserTemperature ut : all) {
            try {
                transactionTemplate.executeWithoutResult(status ->
                        dailyTemperatureRepository.upsert(ut.getUserId(), today, ut.getTemperature()));
            } catch (Exception e) {
                log.warn("일별 온도 스냅샷 실패: userId={}, error={}", ut.getUserId(), e.toString());
            }
        }
        log.info("일별 온도 스냅샷 완료 — 대상 {}명, 기준일 {}", all.size(), today);
    }
}
