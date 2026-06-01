package com.ddiring.ddiring_server.domain.temperature.domain.entity;

import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 사용자의 일별 안부 온도 스냅샷.
 * <p>매일 자정 직전 스케줄러가 그 시점의 누적 온도({@link UserTemperature})를 날짜별로 적재해,
 * 주간 온도 변화 조회에 사용한다. {@code (user_id, record_date)}는 유니크하다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "daily_temperature",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_temperature_user_date",
                columnNames = {"user_id", "record_date"}))
public class DailyTemperature extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_temperature_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(precision = 4, scale = 1, nullable = false)
    private BigDecimal temperature;
}
