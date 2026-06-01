package com.ddiring.ddiring_server.domain.temperature.domain.repository;

import com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature.MAX;
import static com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature.STEP;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserTemperatureRepositoryTest {

    @Autowired
    private UserTemperatureRepository repository;

    @Autowired
    private TestEntityManager em;

    private final LocalDate today = LocalDate.now();

    private Long persistTemperature(BigDecimal temperature, LocalDate lastAttendanceDate) {
        User user = User.builder().name("tester").build();
        em.persist(user);
        em.persist(UserTemperature.builder()
                .user(user)
                .temperature(temperature)
                .lastAttendanceDate(lastAttendanceDate)
                .build());
        em.flush();
        em.clear();
        return user.getId();
    }

    @DisplayName("raiseAttendance 는 온도를 STEP 만큼 원자적으로 올리고 1을 반환한다")
    @Test
    void raise_정상() {
        Long userId = persistTemperature(BigDecimal.valueOf(36.5), null);

        int updated = repository.raiseAttendance(userId, today, STEP, MAX);

        assertThat(updated).isEqualTo(1);
        assertThat(repository.findById(userId).orElseThrow().getTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(37.5));
    }

    @DisplayName("오늘 이미 상승한 타입은 0을 반환하고 온도를 바꾸지 않는다 (일일 멱등)")
    @Test
    void raise_같은날_스킵() {
        Long userId = persistTemperature(BigDecimal.valueOf(36.5), today);

        int updated = repository.raiseAttendance(userId, today, STEP, MAX);

        assertThat(updated).isZero();
        assertThat(repository.findById(userId).orElseThrow().getTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(36.5));
    }

    @DisplayName("어제 상승한 타입은 오늘 다시 상승한다")
    @Test
    void raise_날짜경과_재상승() {
        Long userId = persistTemperature(BigDecimal.valueOf(36.5), today.minusDays(1));

        int updated = repository.raiseAttendance(userId, today, STEP, MAX);

        assertThat(updated).isEqualTo(1);
        assertThat(repository.findById(userId).orElseThrow().getTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(37.5));
    }

    @DisplayName("raiseAttendance 는 상한(MAX)을 넘지 않는다")
    @Test
    void raise_상한() {
        Long userId = persistTemperature(BigDecimal.valueOf(99.5), null);

        int updated = repository.raiseAttendance(userId, today, STEP, MAX);

        assertThat(updated).isEqualTo(1);
        assertThat(repository.findById(userId).orElseThrow().getTemperature())
                .isEqualByComparingTo(MAX);
    }

    @DisplayName("활동 타입이 다르면 같은 날이라도 각각 독립적으로 상승한다")
    @Test
    void raise_타입별_독립() {
        Long userId = persistTemperature(BigDecimal.valueOf(36.5), today); // 오늘 출석 이미 함

        int attendance = repository.raiseAttendance(userId, today, STEP, MAX); // 스킵
        int survey = repository.raiseSurvey(userId, today, STEP, MAX);         // 상승

        assertThat(attendance).isZero();
        assertThat(survey).isEqualTo(1);
        assertThat(repository.findById(userId).orElseThrow().getTemperature())
                .isEqualByComparingTo(BigDecimal.valueOf(37.5));
    }
}
