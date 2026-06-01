package com.ddiring.ddiring_server.domain.temperature.application.service;

import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.family.exception.ElderNotInFamilyException;
import com.ddiring.ddiring_server.domain.family.exception.FamilyMemberNotFoundException;
import com.ddiring.ddiring_server.domain.temperature.application.event.TemperatureRaiseEvent;
import com.ddiring.ddiring_server.domain.temperature.domain.entity.DailyTemperature;
import com.ddiring.ddiring_server.domain.temperature.domain.entity.UserTemperature;
import com.ddiring.ddiring_server.domain.temperature.domain.repository.DailyTemperatureRepository;
import com.ddiring.ddiring_server.domain.temperature.domain.repository.UserTemperatureRepository;
import com.ddiring.ddiring_server.domain.temperature.presentation.dto.response.UserTemperatureResponse;
import com.ddiring.ddiring_server.domain.temperature.presentation.dto.response.WeeklyTemperatureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemperatureService {

    private final UserTemperatureRepository userTemperatureRepository;
    private final DailyTemperatureRepository dailyTemperatureRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional(readOnly = true)
    public UserTemperatureResponse getMyTemperature(Long userId) {
        return userTemperatureRepository.findById(userId)
                .map(UserTemperatureResponse::of)
                .orElseGet(() -> UserTemperatureResponse.defaultFor(userId));
    }

    @Transactional(readOnly = true)
    public WeeklyTemperatureResponse getMyWeeklyTemperature(Long userId) {
        return buildWeekly(userId);
    }

    @Transactional(readOnly = true)
    public WeeklyTemperatureResponse getElderWeeklyTemperature(Long guardianId, Long elderId) {
        validateSameFamily(guardianId, elderId);
        return buildWeekly(elderId);
    }

    /**
     * 지난 주(월~일) 7일간의 일별 온도를 조회한다.
     * 스냅샷 기록이 없는 날은 temperature를 null로 채운다.
     */
    private WeeklyTemperatureResponse buildWeekly(Long userId) {
        LocalDate lastMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1);
        LocalDate lastSunday = lastMonday.plusDays(6);

        Map<LocalDate, BigDecimal> byDate = dailyTemperatureRepository
                .findByUserIdAndDateRange(userId, lastMonday, lastSunday).stream()
                .collect(Collectors.toMap(
                        DailyTemperature::getRecordDate,
                        DailyTemperature::getTemperature,
                        (existing, duplicate) -> duplicate));

        List<WeeklyTemperatureResponse.DailyTemperatureItem> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = lastMonday.plusDays(i);
            days.add(new WeeklyTemperatureResponse.DailyTemperatureItem(
                    date, date.getDayOfWeek(), byDate.get(date)));
        }
        return new WeeklyTemperatureResponse(lastMonday, lastSunday, days);
    }

    private void validateSameFamily(Long guardianId, Long elderId) {
        Long guardianFamilyId = familyMemberRepository.findFamilyIdByUserId(guardianId)
                .orElseThrow(FamilyMemberNotFoundException::new);
        Long elderFamilyId = familyMemberRepository.findFamilyIdByUserId(elderId)
                .orElseThrow(FamilyMemberNotFoundException::new);
        if (!guardianFamilyId.equals(elderFamilyId)) {
            throw new ElderNotInFamilyException();
        }
    }

    /**
     * 활동 시 온도를 상승시킨다.
     * <p>증가/상한/일일 멱등/최초 생성을 DB의 단일 UPSERT로 원자 처리하므로,
     * 같은 사용자의 동시 활동에도 lost update나 중복 키 예외가 발생하지 않는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void raise(TemperatureRaiseEvent event) {
        Long userId = event.actorUserId();
        LocalDate today = LocalDate.now();

        switch (event.actionType()) {
            case ATTENDANCE -> userTemperatureRepository.upsertAttendance(
                    userId, today, UserTemperature.BASE, UserTemperature.STEP, UserTemperature.MAX);
            case SURVEY_ANSWER -> userTemperatureRepository.upsertSurvey(
                    userId, today, UserTemperature.BASE, UserTemperature.STEP, UserTemperature.MAX);
            case PHOTO_VIEW -> userTemperatureRepository.upsertPhotoView(
                    userId, today, UserTemperature.BASE, UserTemperature.STEP, UserTemperature.MAX);
        }
    }
}
