package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.survey.application.cache.WeeklyReportCacheKey;
import com.ddiring.ddiring_server.domain.survey.exception.SurveySessionNotOwnedException;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.WeeklyReportResponse;
import com.ddiring.ddiring_server.global.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final FamilyMemberRepository familyMemberRepository;
    private final WeeklyReportGenerator generator;
    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(Long requesterId, Long elderId, LocalDate startDate, LocalDate endDate) {
        verifyInSameFamily(requesterId, elderId);
        return generator.generate(elderId, startDate, endDate);
    }

    /** 스케줄러 등 권한 검증이 필요 없는 내부 호출용 진입점. */
    public WeeklyReportResponse generateForElder(Long elderId, LocalDate startDate, LocalDate endDate) {
        return generator.generate(elderId, startDate, endDate);
    }

    /**
     * 어르신이 새 설문에 응답하면, 해당 날짜를 포함하는 기간의 리포트 캐시만 선택적으로 제거한다.
     * 이미 종료된 과거 주는 영향을 받지 않으므로 그대로 둔다.
     */
    public void evictReportsContaining(Long elderId, LocalDate date) {
        Cache cache = cacheManager.getCache(CacheConfig.WEEKLY_REPORT_CACHE);
        if (cache == null) {
            return;
        }
        if (!(cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache<?, ?> caffeine)) {
            return;
        }
        caffeine.asMap().keySet().removeIf(k ->
                k instanceof WeeklyReportCacheKey key
                        && key.elderId().equals(elderId)
                        && !date.isBefore(key.startDate())
                        && !date.isAfter(key.endDate()));
    }

    private void verifyInSameFamily(Long requesterId, Long elderId) {
        Long requesterFamilyId = familyMemberRepository.findFamilyIdByUserId(requesterId)
                .orElseThrow(SurveySessionNotOwnedException::new);
        Long elderFamilyId = familyMemberRepository.findFamilyIdByUserId(elderId)
                .orElseThrow(SurveySessionNotOwnedException::new);
        if (!requesterFamilyId.equals(elderFamilyId)) {
            throw new SurveySessionNotOwnedException();
        }
    }
}
