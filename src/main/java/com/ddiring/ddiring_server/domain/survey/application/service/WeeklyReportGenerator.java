package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.WeeklyReportResponse;
import com.ddiring.ddiring_server.global.client.fastapi.FastApiSurveyClient;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportRequest;
import com.ddiring.ddiring_server.global.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 주간 리포트 생성(DB 집계 + FastAPI 호출)을 담당하는 빈.
 * 캐시가 적용된 {@link #generate} 가 프록시를 타도록 {@link WeeklyReportService}와 분리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportGenerator {

    private final SurveySessionRepository sessionRepository;
    private final SurveyAnswerRepository answerRepository;
    private final FastApiSurveyClient fastApiSurveyClient;

    /**
     * (elderId, startDate, endDate) 단위로 1일간 캐싱한다.
     * 생성에 실패해(report null) fallback 응답이 나온 경우엔 캐싱하지 않는다.
     */
    @Cacheable(
            cacheNames = CacheConfig.WEEKLY_REPORT_CACHE,
            key = "new com.ddiring.ddiring_server.domain.survey.application.cache.WeeklyReportCacheKey(#elderId, #startDate, #endDate)",
            unless = "#result == null || #result.report() == null"
    )
    @Transactional(readOnly = true)
    public WeeklyReportResponse generate(Long elderId, LocalDate startDate, LocalDate endDate) {
        List<SurveySession> sessions = sessionRepository.findCompletedByElderIdAndDateRange(elderId, startDate, endDate);

        String elderName = sessions.isEmpty() ? "" : sessions.get(0).getElder().getName();

        List<WeeklyReportRequest.DailyResponse> weeklyResponses = sessions.stream()
                .map(session -> {
                    Map<String, String> responses = buildResponsesMap(session.getId());
                    return new WeeklyReportRequest.DailyResponse(
                            session.getSessionDate().toString(),
                            responses
                    );
                })
                .filter(dr -> !dr.responses().isEmpty())
                .toList();

        WeeklyReportRequest request = new WeeklyReportRequest(
                new WeeklyReportRequest.ElderProfile(elderName),
                startDate.toString(),
                endDate.toString(),
                weeklyResponses
        );

        Optional<com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse> result =
                fastApiSurveyClient.getWeeklyReport(request);

        if (result.isEmpty()) {
            log.warn("주간 리포트 생성 실패: elderId={}, {}~{}", elderId, startDate, endDate);
            return new WeeklyReportResponse(elderId, elderName, startDate, endDate, null, null);
        }

        com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse fastApiResponse = result.get();
        return new WeeklyReportResponse(
                elderId,
                elderName,
                startDate,
                endDate,
                fastApiResponse.report(),
                fastApiResponse.patterns()
        );
    }

    private Map<String, String> buildResponsesMap(Long sessionId) {
        List<Object[]> rows = answerRepository.findCategoryAnswersBySessionId(sessionId);
        Map<String, String> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.putIfAbsent((String) row[0], (String) row[1]);
        }
        return map;
    }
}
