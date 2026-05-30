package com.ddiring.ddiring_server.domain.distance.application.event;

import com.ddiring.ddiring_server.domain.distance.application.service.DistanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistanceEventListener {

    private final DistanceService distanceService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDistanceReset(DistanceResetEvent event) {
        try {
            distanceService.handleReset(event);
        } catch (Exception e) {
            log.warn("거리 리셋 처리 실패: event={}, error={}", event, e.toString());
        }
    }
}
