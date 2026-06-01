package com.ddiring.ddiring_server.domain.temperature.domain.repository;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface UserTemperatureRepository extends JpaRepository<UserTemperature, Long> {

    Optional<UserTemperature> findByUserId(Long userId);

    /**
     * 출석 활동에 대해 온도를 원자적으로 상승시킨다.
     * - 증가/상한 적용을 DB에서 단일 UPDATE로 처리해 lost update 방지.
     * - 오늘 이미 같은 활동으로 상승한 경우(WHERE 미충족) 0행을 반환해 중복 상승을 막는다.
     *
     * @return 갱신된 행 수 (1=상승함, 0=row 없음 또는 오늘 이미 상승함)
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE user_temperature SET temperature = LEAST(temperature + :step, :max), " +
            "last_attendance_date = :today, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = :userId AND (last_attendance_date IS NULL OR last_attendance_date <> :today)",
            nativeQuery = true)
    int raiseAttendance(@Param("userId") Long userId, @Param("today") LocalDate today,
                        @Param("step") BigDecimal step, @Param("max") BigDecimal max);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE user_temperature SET temperature = LEAST(temperature + :step, :max), " +
            "last_survey_date = :today, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = :userId AND (last_survey_date IS NULL OR last_survey_date <> :today)",
            nativeQuery = true)
    int raiseSurvey(@Param("userId") Long userId, @Param("today") LocalDate today,
                    @Param("step") BigDecimal step, @Param("max") BigDecimal max);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE user_temperature SET temperature = LEAST(temperature + :step, :max), " +
            "last_photo_view_date = :today, updated_at = CURRENT_TIMESTAMP " +
            "WHERE user_id = :userId AND (last_photo_view_date IS NULL OR last_photo_view_date <> :today)",
            nativeQuery = true)
    int raisePhotoView(@Param("userId") Long userId, @Param("today") LocalDate today,
                       @Param("step") BigDecimal step, @Param("max") BigDecimal max);
}
