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

import static com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature.BASE;
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

    /** 온도 row 없이 User만 생성하고 userId 반환 (최초 활동 = INSERT 경로 검증용) */
    private Long persistUserOnly() {
        User user = User.builder().name("tester").build();
        em.persist(user);
        em.flush();
        em.clear();
        return user.getId();
    }

    /** User + 지정 온도/마지막 출석일의 온도 row를 함께 생성 (UPDATE 경로 검증용) */
    private Long persistWithTemperature(BigDecimal temperature, LocalDate lastAttendanceDate) {
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

    private BigDecimal temperatureOf(Long userId) {
        return repository.findById(userId).orElseThrow().getTemperature();
    }

    @DisplayName("최초 활동 시 row가 없으면 BASE+STEP(37.5)으로 INSERT 된다")
    @Test
    void upsert_최초_INSERT() {
        Long userId = persistUserOnly();

        repository.upsertAttendance(userId, today, BASE, STEP, MAX);

        assertThat(temperatureOf(userId)).isEqualByComparingTo(BigDecimal.valueOf(37.5));
    }

    @DisplayName("기존 row가 있으면 STEP 만큼 UPDATE 된다")
    @Test
    void upsert_기존_UPDATE() {
        Long userId = persistWithTemperature(BigDecimal.valueOf(36.5), null);

        repository.upsertAttendance(userId, today, BASE, STEP, MAX);

        assertThat(temperatureOf(userId)).isEqualByComparingTo(BigDecimal.valueOf(37.5));
    }

    @DisplayName("오늘 이미 상승한 타입은 다시 호출해도 온도가 변하지 않는다 (일일 멱등)")
    @Test
    void upsert_같은날_멱등() {
        Long userId = persistWithTemperature(BigDecimal.valueOf(36.5), today);

        repository.upsertAttendance(userId, today, BASE, STEP, MAX);

        assertThat(temperatureOf(userId)).isEqualByComparingTo(BigDecimal.valueOf(36.5));
    }

    @DisplayName("어제 상승한 타입은 오늘 다시 상승한다")
    @Test
    void upsert_날짜경과_재상승() {
        Long userId = persistWithTemperature(BigDecimal.valueOf(36.5), today.minusDays(1));

        repository.upsertAttendance(userId, today, BASE, STEP, MAX);

        assertThat(temperatureOf(userId)).isEqualByComparingTo(BigDecimal.valueOf(37.5));
    }

    @DisplayName("상한(MAX)을 넘지 않는다")
    @Test
    void upsert_상한() {
        Long userId = persistWithTemperature(BigDecimal.valueOf(99.5), null);

        repository.upsertAttendance(userId, today, BASE, STEP, MAX);

        assertThat(temperatureOf(userId)).isEqualByComparingTo(MAX);
    }

    @DisplayName("활동 타입이 다르면 같은 날이라도 각각 독립적으로 상승한다")
    @Test
    void upsert_타입별_독립() {
        Long userId = persistWithTemperature(BigDecimal.valueOf(36.5), today); // 오늘 출석 이미 함

        repository.upsertAttendance(userId, today, BASE, STEP, MAX); // 멱등 스킵
        repository.upsertSurvey(userId, today, BASE, STEP, MAX);     // 상승

        assertThat(temperatureOf(userId)).isEqualByComparingTo(BigDecimal.valueOf(37.5));
    }
}
