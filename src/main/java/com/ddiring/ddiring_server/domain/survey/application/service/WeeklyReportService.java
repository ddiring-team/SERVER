package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.SurveyCategory;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.domain.survey.exception.SurveySessionNotOwnedException;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.CategoryStatus;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.CategorySummary;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.WeeklyReportResponse;
import com.ddiring.ddiring_server.global.client.fastapi.FastApiSurveyClient;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportRequest;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse.Pattern;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse.Stats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final SurveySessionRepository sessionRepository;
    private final SurveyAnswerRepository answerRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FastApiSurveyClient fastApiSurveyClient;

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(Long requesterId, Long elderId, LocalDate startDate, LocalDate endDate) {
        verifyInSameFamily(requesterId, elderId);
        return generateForElder(elderId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public WeeklyReportResponse generateForElder(Long elderId, LocalDate startDate, LocalDate endDate) {
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
            return new WeeklyReportResponse(elderId, elderName, startDate, endDate,
                    null, null, buildCategorySummaries(null, null));
        }

        com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse fastApiResponse = result.get();
        return new WeeklyReportResponse(
                elderId,
                elderName,
                startDate,
                endDate,
                fastApiResponse.report(),
                fastApiResponse.patterns(),
                buildCategorySummaries(fastApiResponse.patterns(), fastApiResponse.stats())
        );
    }

    /** 고정 노출 카테고리 (화면 카드 순서). */
    private static final List<SurveyCategory> SUMMARY_CATEGORIES = List.of(
            SurveyCategory.MEAL,
            SurveyCategory.HEALTH,
            SurveyCategory.MOOD,
            SurveyCategory.ACTIVITY
    );

    /**
     * AI patterns(우려 패턴) + 응답 유무(stats.by_category)를 결합해 카테고리별 상태 카드를 만든다.
     * 점수화 대신, AI가 검증을 거쳐 산출한 patterns의 severity를 그대로 사용한다.
     */
    private List<CategorySummary> buildCategorySummaries(List<Pattern> patterns, Stats stats) {
        Map<String, Object> byCategory = (stats == null || stats.byCategory() == null)
                ? Map.of() : stats.byCategory();
        Map<String, Pattern> worstByCategory = worstPatternByCategory(patterns);

        return SUMMARY_CATEGORIES.stream()
                .map(category -> {
                    String displayName = category.getDisplayName();
                    int responseDays = responseDaysOf(byCategory.get(displayName));
                    if (responseDays == 0) {
                        return CategorySummary.noData(category);
                    }
                    Pattern pattern = worstByCategory.get(displayName);
                    CategoryStatus status = statusOf(pattern);
                    return new CategorySummary(
                            category.name(),
                            displayName,
                            status.name(),
                            status.getLabel(),
                            pattern == null ? null : pattern.observation(),
                            pattern == null ? null : pattern.severity(),
                            responseDays
                    );
                })
                .toList();
    }

    /** 카테고리별로 가장 심각한 패턴 1건을 추린다. */
    private Map<String, Pattern> worstPatternByCategory(List<Pattern> patterns) {
        if (patterns == null) {
            return Map.of();
        }
        Map<String, Pattern> map = new HashMap<>();
        for (Pattern pattern : patterns) {
            if (pattern.category() == null) {
                continue;
            }
            map.merge(pattern.category(), pattern, (existing, candidate) ->
                    severityRank(candidate.severity()) > severityRank(existing.severity())
                            ? candidate : existing);
        }
        return map;
    }

    private CategoryStatus statusOf(Pattern pattern) {
        if (pattern == null) {
            return CategoryStatus.GOOD;
        }
        return severityRank(pattern.severity()) >= 2 ? CategoryStatus.ATTENTION : CategoryStatus.INFO;
    }

    private int severityRank(String severity) {
        if (severity == null) {
            return 1;
        }
        return switch (severity.toLowerCase()) {
            case "danger", "critical" -> 3;
            case "warning" -> 2;
            default -> 1; // info 등
        };
    }

    /** stats.by_category[키].value_counts 값들의 합 = 해당 카테고리 응답 일수. */
    @SuppressWarnings("unchecked")
    private int responseDaysOf(Object entry) {
        if (!(entry instanceof Map<?, ?> categoryStat)) {
            return 0;
        }
        Object valueCounts = ((Map<String, Object>) categoryStat).get("value_counts");
        if (!(valueCounts instanceof Map<?, ?> counts)) {
            return 0;
        }
        int sum = 0;
        for (Object value : counts.values()) {
            if (value instanceof Number number) {
                sum += number.intValue();
            }
        }
        return sum;
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

    private Map<String, String> buildResponsesMap(Long sessionId) {
        List<Object[]> rows = answerRepository.findCategoryAnswersBySessionId(sessionId);
        Map<String, String> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.putIfAbsent((String) row[0], (String) row[1]);
        }
        return map;
    }
}
