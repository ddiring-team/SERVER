package com.ddiring.ddiring_server.domain.temperature.domain.repository;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface UserTemperatureRepository extends JpaRepository<UserTemperature, Long> {

    /**
     * 출석 활동에 대해 온도를 원자적으로 상승시킨다 (UPSERT).
     * <p>row가 없으면 BASE+STEP으로 INSERT, 있으면 LEAST(temperature+STEP, MAX)로 UPDATE한다.
     * 오늘 이미 같은 활동으로 상승한 경우(CASE WHEN 미충족) 온도를 그대로 두어 일일 멱등을 보장한다.
     * INSERT/UPDATE를 단일 쿼리로 처리해 동시성 경합 시에도 예외·lost update 없이 안전하다.
     * <p>{@code last_attendance_date} 할당을 마지막에 두어 temperature/updated_at CASE가
     * 갱신 전 날짜를 참조하도록 한다 (MySQL은 좌→우 평가).
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO user_temperature (user_id, temperature, last_attendance_date, created_at, updated_at) " +
            "VALUES (:userId, :base + :step, :today, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON DUPLICATE KEY UPDATE " +
            "temperature = CASE WHEN last_attendance_date IS NULL OR last_attendance_date <> :today THEN LEAST(temperature + :step, :max) ELSE temperature END, " +
            "updated_at = CASE WHEN last_attendance_date IS NULL OR last_attendance_date <> :today THEN CURRENT_TIMESTAMP ELSE updated_at END, " +
            "last_attendance_date = :today",
            nativeQuery = true)
    void upsertAttendance(@Param("userId") Long userId, @Param("today") LocalDate today,
                          @Param("base") BigDecimal base, @Param("step") BigDecimal step, @Param("max") BigDecimal max);

    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO user_temperature (user_id, temperature, last_survey_date, created_at, updated_at) " +
            "VALUES (:userId, :base + :step, :today, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON DUPLICATE KEY UPDATE " +
            "temperature = CASE WHEN last_survey_date IS NULL OR last_survey_date <> :today THEN LEAST(temperature + :step, :max) ELSE temperature END, " +
            "updated_at = CASE WHEN last_survey_date IS NULL OR last_survey_date <> :today THEN CURRENT_TIMESTAMP ELSE updated_at END, " +
            "last_survey_date = :today",
            nativeQuery = true)
    void upsertSurvey(@Param("userId") Long userId, @Param("today") LocalDate today,
                      @Param("base") BigDecimal base, @Param("step") BigDecimal step, @Param("max") BigDecimal max);

    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO user_temperature (user_id, temperature, last_photo_view_date, created_at, updated_at) " +
            "VALUES (:userId, :base + :step, :today, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
            "ON DUPLICATE KEY UPDATE " +
            "temperature = CASE WHEN last_photo_view_date IS NULL OR last_photo_view_date <> :today THEN LEAST(temperature + :step, :max) ELSE temperature END, " +
            "updated_at = CASE WHEN last_photo_view_date IS NULL OR last_photo_view_date <> :today THEN CURRENT_TIMESTAMP ELSE updated_at END, " +
            "last_photo_view_date = :today",
            nativeQuery = true)
    void upsertPhotoView(@Param("userId") Long userId, @Param("today") LocalDate today,
                         @Param("base") BigDecimal base, @Param("step") BigDecimal step, @Param("max") BigDecimal max);
}
