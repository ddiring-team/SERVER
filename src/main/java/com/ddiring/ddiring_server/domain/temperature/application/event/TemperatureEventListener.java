package com.ddiring.ddiring_server.domain.temperature.application.event;

import com.ddiring.ddiring_server.domain.temperature.application.service.TemperatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureEventListener {

    private final TemperatureService temperatureService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTemperatureRaise(TemperatureRaiseEvent event) {
        try {
            temperatureService.raise(event);
        } catch (Exception e) {
            log.warn("안부 온도 상승 처리 실패: event={}, error={}", event, e.toString());
        }
    }
}
