package com.ddiring.ddiring_server.domain.temperature.domain.entity;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.enums.TemperatureActionType;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user_temperature")
public class UserTemperature extends BaseEntity {

    public static final BigDecimal BASE = BigDecimal.valueOf(36.5);
    public static final BigDecimal STEP = BigDecimal.valueOf(1.0);
    public static final BigDecimal MAX = BigDecimal.valueOf(100.0);

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @Column(precision = 4, scale = 1, nullable = false)
    private BigDecimal temperature = BASE;

    private LocalDate lastAttendanceDate;   // ATTENDANCE 마지막 상승일
    private LocalDate lastSurveyDate;       // SURVEY_ANSWER 마지막 상승일
    private LocalDate lastPhotoViewDate;    // PHOTO_VIEW 마지막 상승일

    /**
     * 온도 row가 아직 없는 사용자의 첫 활동 시 초기 row를 생성한다.
     * 기본 온도(BASE)에서 1회 상승(STEP, 상한 MAX)을 반영하고 해당 활동일을 기록한다.
     * 이후 상승은 {@code UserTemperatureRepository}의 원자적 UPDATE로 처리한다.
     */
    public static UserTemperature createInitial(User user, TemperatureActionType actionType, LocalDate today) {
        UserTemperature temperature = UserTemperature.builder()
                .user(user)
                .temperature(BASE.add(STEP).min(MAX))
                .build();
        temperature.markRaised(actionType, today);
        return temperature;
    }

    private void markRaised(TemperatureActionType actionType, LocalDate today) {
        switch (actionType) {
            case ATTENDANCE -> lastAttendanceDate = today;
            case SURVEY_ANSWER -> lastSurveyDate = today;
            case PHOTO_VIEW -> lastPhotoViewDate = today;
        }
    }
}
