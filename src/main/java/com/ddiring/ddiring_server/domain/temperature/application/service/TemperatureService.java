package com.ddiring.ddiring_server.domain.temperature.application.service;

import com.ddiring.ddiring_server.domain.temperature.application.event.TemperatureRaiseEvent;
import com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature;
import com.ddiring.ddiring_server.domain.temperature.domain.repository.UserTemperatureRepository;
import com.ddiring.ddiring_server.domain.temperature.presentation.dto.response.UserTemperatureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TemperatureService {

    private final UserTemperatureRepository userTemperatureRepository;

    @Transactional(readOnly = true)
    public UserTemperatureResponse getMyTemperature(Long userId) {
        return userTemperatureRepository.findById(userId)
                .map(UserTemperatureResponse::of)
                .orElseGet(() -> UserTemperatureResponse.defaultFor(userId));
    }

    /**
     * 활동 시 온도를 상승시킨다.
     * <p>증가/상한/일일 멱등/최초 생성을 DB의 단일 UPSERT로 원자 처리하므로,
     * 같은 사용자의 동시 활동에도 lost update나 중복 키 예외가 발생하지 않는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void raise(TemperatureRaiseEvent event) {
        Long userId = event.actorUserId();
        LocalDate today = LocalDate.now();

        switch (event.actionType()) {
            case ATTENDANCE -> userTemperatureRepository.upsertAttendance(
                    userId, today, UserTemperature.BASE, UserTemperature.STEP, UserTemperature.MAX);
            case SURVEY_ANSWER -> userTemperatureRepository.upsertSurvey(
                    userId, today, UserTemperature.BASE, UserTemperature.STEP, UserTemperature.MAX);
            case PHOTO_VIEW -> userTemperatureRepository.upsertPhotoView(
                    userId, today, UserTemperature.BASE, UserTemperature.STEP, UserTemperature.MAX);
        }
    }
}
