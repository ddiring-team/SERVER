package com.ddiring.ddiring_server.domain.temperature.domain.repository;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.DailyTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DailyTemperatureRepository extends JpaRepository<DailyTemperature, Long> {

    /**
     * 특정 날짜의 일별 온도 스냅샷을 원자적으로 적재한다 (UPSERT).
     * <p>같은 날 스케줄러가 재실행되어도 온도를 최신 값으로 덮어쓰며 중복 행을 만들지 않는다.
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO daily_temperature (user_id, record_date, temperature, created_at, updated_at) " +
            "VALUES (:userId, :date, :temperature, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON DUPLICATE KEY UPDATE temperature = :temperature, updated_at = CURRENT_TIMESTAMP",
            nativeQuery = true)
    void upsert(@Param("userId") Long userId, @Param("date") LocalDate date,
                @Param("temperature") BigDecimal temperature);

    @Query("SELECT dt FROM DailyTemperature dt WHERE dt.userId = :userId " +
            "AND dt.recordDate BETWEEN :start AND :end ORDER BY dt.recordDate ASC")
    List<DailyTemperature> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                    @Param("start") LocalDate start,
                                                    @Param("end") LocalDate end);
}
