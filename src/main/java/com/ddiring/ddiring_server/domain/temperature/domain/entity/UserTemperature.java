package com.ddiring.ddiring_server.domain.temperature.domain.entity;

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
}
