package com.ddiring.ddiring_server.domain.alert.application.service;

import com.ddiring.ddiring_server.domain.alert.domain.entity.NotificationSetting;
import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.NotificationType;
import com.ddiring.ddiring_server.domain.alert.domain.repository.NotificationSettingRepository;
import com.ddiring.ddiring_server.domain.alert.presentation.dto.response.NotificationSettingResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 전체 알림 설정을 조회한다. 저장된 행이 없는 타입은 기본 ON(true)으로 채워 모든 타입을 반환한다.
     */
    @Transactional(readOnly = true)
    public List<NotificationSettingResponse> getMySettings(Long userId) {
        Map<NotificationType, Boolean> enabledByType = new EnumMap<>(NotificationType.class);
        for (NotificationSetting setting : notificationSettingRepository.findAllByUser_Id(userId)) {
            enabledByType.put(setting.getType(), setting.isEnabled());
        }

        return Arrays.stream(NotificationType.values())
                .map(type -> NotificationSettingResponse.of(type, enabledByType.getOrDefault(type, true)))
                .toList();
    }

    /**
     * 사용자의 특정 알림 타입 on/off 를 갱신한다. 행이 없으면 새로 생성(upsert)한다.
     */
    @Transactional
    public NotificationSettingResponse updateSetting(Long userId, NotificationType type, boolean enabled) {
        NotificationSetting setting = notificationSettingRepository.findByUser_IdAndType(userId, type)
                .orElse(null);

        if (setting == null) {
            User user = userRepository.getReferenceById(userId);
            setting = notificationSettingRepository.save(
                    NotificationSetting.builder()
                            .user(user)
                            .type(type)
                            .enabled(enabled)
                            .build()
            );
        } else {
            setting.updateEnabled(enabled);
        }

        return NotificationSettingResponse.of(type, enabled);
    }

    /**
     * 발송 측에서 단일 사용자의 알림 수신 여부를 확인한다. 설정 행이 없으면 기본 ON(true).
     */
    @Transactional(readOnly = true)
    public boolean isEnabled(Long userId, NotificationType type) {
        return notificationSettingRepository.findByUser_IdAndType(userId, type)
                .map(NotificationSetting::isEnabled)
                .orElse(true);
    }
}
