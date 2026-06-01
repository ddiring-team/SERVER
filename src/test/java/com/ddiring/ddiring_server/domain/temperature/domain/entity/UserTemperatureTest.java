package com.ddiring.ddiring_server.domain.temperature.domain.entity;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.enums.TemperatureActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserTemperatureTest {

    @DisplayName("createInitial 은 기본 온도에서 1회 상승(37.5)하고 해당 활동일만 기록한다")
    @Test
    void createInitial_정상() {
        LocalDate today = LocalDate.now();

        UserTemperature ut = UserTemperature.createInitial(null, TemperatureActionType.ATTENDANCE, today);

        assertThat(ut.getTemperature()).isEqualByComparingTo(BigDecimal.valueOf(37.5));
        assertThat(ut.getLastAttendanceDate()).isEqualTo(today);
        assertThat(ut.getLastSurveyDate()).isNull();
        assertThat(ut.getLastPhotoViewDate()).isNull();
    }

    @DisplayName("createInitial 은 활동 타입에 해당하는 날짜 컬럼만 채운다")
    @Test
    void createInitial_타입별_날짜기록() {
        LocalDate today = LocalDate.now();

        UserTemperature ut = UserTemperature.createInitial(null, TemperatureActionType.PHOTO_VIEW, today);

        assertThat(ut.getLastPhotoViewDate()).isEqualTo(today);
        assertThat(ut.getLastAttendanceDate()).isNull();
        assertThat(ut.getLastSurveyDate()).isNull();
    }
}
