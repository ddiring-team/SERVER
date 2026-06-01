package com.ddiring.ddiring_server.domain.temperature.application.service;

import com.ddiring.ddiring_server.domain.temperature.application.event.TemperatureRaiseEvent;
import com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature;
import com.ddiring.ddiring_server.domain.temperature.domain.entity.enums.TemperatureActionType;
import com.ddiring.ddiring_server.domain.temperature.domain.repository.UserTemperatureRepository;
import com.ddiring.ddiring_server.domain.temperature.presentation.dto.response.UserTemperatureResponse;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TemperatureService {

    private final UserTemperatureRepository userTemperatureRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserTemperatureResponse getMyTemperature(Long userId) {
        return userTemperatureRepository.findByUserId(userId)
                .map(UserTemperatureResponse::of)
                .orElseGet(() -> UserTemperatureResponse.defaultFor(userId));
    }

    /**
     * 활동 시 온도를 상승시킨다.
     * <p>온도 증가/상한/일일 멱등을 DB의 원자적 UPDATE 한 번으로 처리해 동시 활동 간 lost update를 막는다.
     * row가 아직 없으면 초기 row를 생성하며, 동시 생성으로 인한 PK 충돌은 잡아 UPDATE로 재시도한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void raise(TemperatureRaiseEvent event) {
        Long userId = event.actorUserId();
        TemperatureActionType actionType = event.actionType();
        LocalDate today = LocalDate.now();

        int updated = raiseAtomically(userId, actionType, today);
        if (updated > 0) {
            return; // 정상 상승
        }
        if (userTemperatureRepository.existsById(userId)) {
            return; // row는 있으나 오늘 이미 해당 타입으로 상승함 → 멱등 스킵
        }
        try {
            userTemperatureRepository.save(UserTemperature.createInitial(
                    userRepository.getReferenceById(userId), actionType, today));
        } catch (DataIntegrityViolationException e) {
            // 다른 트랜잭션이 먼저 row를 생성함 → 원자적 UPDATE로 재시도
            raiseAtomically(userId, actionType, today);
        }
    }

    private int raiseAtomically(Long userId, TemperatureActionType actionType, LocalDate today) {
        return switch (actionType) {
            case ATTENDANCE -> userTemperatureRepository.raiseAttendance(
                    userId, today, UserTemperature.STEP, UserTemperature.MAX);
            case SURVEY_ANSWER -> userTemperatureRepository.raiseSurvey(
                    userId, today, UserTemperature.STEP, UserTemperature.MAX);
            case PHOTO_VIEW -> userTemperatureRepository.raisePhotoView(
                    userId, today, UserTemperature.STEP, UserTemperature.MAX);
        };
    }
}
