package com.ddiring.ddiring_server.domain.distance.domain.entity;

import com.ddiring.ddiring_server.domain.distance.domain.entity.enums.DistanceActionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class PairDistanceTest {

    @DisplayName("increment 는 거리를 1 늘리고 true 를 반환한다")
    @Test
    void increment_정상() {
        PairDistance pd = PairDistance.builder().distanceKm(3).build();

        boolean changed = pd.increment();

        assertThat(changed).isTrue();
        assertThat(pd.getDistanceKm()).isEqualTo(4);
    }

    @DisplayName("increment 는 상한(10km)에 도달하면 더 늘리지 않고 false 를 반환한다")
    @Test
    void increment_포화() {
        PairDistance pd = PairDistance.builder().distanceKm(PairDistance.MAX_DISTANCE_KM).build();

        boolean changed = pd.increment();

        assertThat(changed).isFalse();
        assertThat(pd.getDistanceKm()).isEqualTo(PairDistance.MAX_DISTANCE_KM);
    }

    @DisplayName("reset 은 거리를 0 으로 초기화하고 lastAction 을 설정하며 maxReachedNotifiedAt 을 비운다")
    @Test
    void reset_정상() {
        PairDistance pd = PairDistance.builder().distanceKm(10).build();
        ReflectionTestUtils.setField(pd, "maxReachedNotifiedAt", java.time.LocalDateTime.now());

        pd.reset(DistanceActionType.ATTENDANCE);

        assertThat(pd.getDistanceKm()).isZero();
        assertThat(pd.getLastActionAt()).isNotNull();
        assertThat(pd.getLastActionType()).isEqualTo(DistanceActionType.ATTENDANCE);
        assertThat(pd.getMaxReachedNotifiedAt()).isNull();
    }

    @DisplayName("shouldNotifyMax 는 10km 도달 + 미알림 상태에서만 true")
    @Test
    void shouldNotifyMax() {
        PairDistance notReached = PairDistance.builder().distanceKm(9).build();
        PairDistance reached = PairDistance.builder().distanceKm(10).build();
        PairDistance reachedAndNotified = PairDistance.builder().distanceKm(10).build();
        reachedAndNotified.markMaxNotified();

        assertThat(notReached.shouldNotifyMax()).isFalse();
        assertThat(reached.shouldNotifyMax()).isTrue();
        assertThat(reachedAndNotified.shouldNotifyMax()).isFalse();
    }
}
