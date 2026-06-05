package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.CategorySummary;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.WeeklyReportResponse;
import com.ddiring.ddiring_server.global.client.fastapi.FastApiSurveyClient;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse.Pattern;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse.Stats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WeeklyReportServiceTest {

    @Mock
    private SurveySessionRepository sessionRepository;

    @Mock
    private SurveyAnswerRepository answerRepository;

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private FastApiSurveyClient fastApiSurveyClient;

    @InjectMocks
    private WeeklyReportService weeklyReportService;

    private static final long ELDER_ID = 1L;
    private static final LocalDate START = LocalDate.of(2026, 5, 14);
    private static final LocalDate END = LocalDate.of(2026, 5, 20);

    @DisplayName("응답이 있고 warning 패턴이 있는 카테고리는 ATTENTION 상태가 된다")
    @Test
    void categorySummary_warning패턴_ATTENTION() {
        Pattern mealWarning = new Pattern("식사 / 수분", "주중 식사를 거르신 날이 2회 관찰됨",
                "warning", List.of("2026-05-15", "2026-05-18"));
        givenFastApiResponse(
                List.of(mealWarning),
                byCategory("식사 / 수분", Map.of("예", 5, "아니요", 2))
        );

        CategorySummary meal = summaryOf(callService(), "MEAL");

        assertThat(meal.status()).isEqualTo("ATTENTION");
        assertThat(meal.statusLabel()).isEqualTo("주의 필요");
        assertThat(meal.severity()).isEqualTo("warning");
        assertThat(meal.note()).isEqualTo("주중 식사를 거르신 날이 2회 관찰됨");
        assertThat(meal.responseDays()).isEqualTo(7);
    }

    @DisplayName("응답이 있고 패턴이 없는 카테고리는 GOOD 상태가 된다")
    @Test
    void categorySummary_패턴없음_GOOD() {
        givenFastApiResponse(
                List.of(),
                byCategory("건강 상태", Map.of("괜찮아요", 4, "조금 불편", 3))
        );

        CategorySummary health = summaryOf(callService(), "HEALTH");

        assertThat(health.status()).isEqualTo("GOOD");
        assertThat(health.statusLabel()).isEqualTo("양호");
        assertThat(health.note()).isNull();
        assertThat(health.severity()).isNull();
        assertThat(health.responseDays()).isEqualTo(7);
    }

    @DisplayName("info 패턴만 있는 카테고리는 INFO 상태가 된다")
    @Test
    void categorySummary_info패턴_INFO() {
        Pattern moodInfo = new Pattern("기분 / 감정", "전반적으로 안정적", "info", List.of("2026-05-16"));
        givenFastApiResponse(
                List.of(moodInfo),
                byCategory("기분 / 감정", Map.of("보통", 5, "외로움", 2))
        );

        CategorySummary mood = summaryOf(callService(), "MOOD");

        assertThat(mood.status()).isEqualTo("INFO");
        assertThat(mood.statusLabel()).isEqualTo("참고");
        assertThat(mood.severity()).isEqualTo("info");
    }

    @DisplayName("해당 주 응답이 없는 카테고리는 NO_DATA 상태가 된다")
    @Test
    void categorySummary_응답없음_NO_DATA() {
        givenFastApiResponse(
                List.of(),
                byCategory("식사 / 수분", Map.of("예", 3)) // 활동/외출은 응답 없음
        );

        CategorySummary activity = summaryOf(callService(), "ACTIVITY");

        assertThat(activity.status()).isEqualTo("NO_DATA");
        assertThat(activity.statusLabel()).isEqualTo("기록 없음");
        assertThat(activity.responseDays()).isZero();
    }

    @DisplayName("한 카테고리에 info와 warning이 함께 있으면 가장 심각한 ATTENTION이 된다")
    @Test
    void categorySummary_복수패턴_최고severity선택() {
        Pattern info = new Pattern("식사 / 수분", "관찰", "info", List.of("2026-05-14"));
        Pattern warning = new Pattern("식사 / 수분", "거르심", "warning", List.of("2026-05-15"));
        givenFastApiResponse(
                List.of(info, warning),
                byCategory("식사 / 수분", Map.of("예", 4, "아니요", 3))
        );

        CategorySummary meal = summaryOf(callService(), "MEAL");

        assertThat(meal.status()).isEqualTo("ATTENTION");
        assertThat(meal.note()).isEqualTo("거르심");
    }

    @DisplayName("AI 호출이 실패하면 모든 카테고리가 NO_DATA로 채워지고 report는 null이다")
    @Test
    void categorySummary_AI실패_모두NO_DATA() {
        given(sessionRepository.findCompletedByElderIdAndDateRange(ELDER_ID, START, END))
                .willReturn(List.of());
        given(fastApiSurveyClient.getWeeklyReport(any())).willReturn(Optional.empty());

        WeeklyReportResponse response = callService();

        assertThat(response.report()).isNull();
        assertThat(response.categorySummaries()).hasSize(4)
                .allSatisfy(summary -> assertThat(summary.status()).isEqualTo("NO_DATA"));
    }

    @DisplayName("카테고리 카드는 항상 MEAL/HEALTH/MOOD/ACTIVITY 4개가 고정 순서로 내려간다")
    @Test
    void categorySummary_고정순서_4개() {
        givenFastApiResponse(List.of(), byCategory("식사 / 수분", Map.of("예", 1)));

        List<CategorySummary> summaries = callService().categorySummaries();

        assertThat(summaries).extracting(CategorySummary::category)
                .containsExactly("MEAL", "HEALTH", "MOOD", "ACTIVITY");
    }

    // --- helpers ---

    private WeeklyReportResponse callService() {
        return weeklyReportService.generateForElder(ELDER_ID, START, END);
    }

    private void givenFastApiResponse(List<Pattern> patterns, Map<String, Object> byCategory) {
        given(sessionRepository.findCompletedByElderIdAndDateRange(ELDER_ID, START, END))
                .willReturn(List.of());
        Stats stats = new Stats(List.of(), null, byCategory);
        com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse fastApiResponse =
                new com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse(
                        "주간 리포트 본문", patterns, stats);
        given(fastApiSurveyClient.getWeeklyReport(any())).willReturn(Optional.of(fastApiResponse));
    }

    private Map<String, Object> byCategory(String displayName, Map<String, Integer> valueCounts) {
        return Map.of(displayName, Map.of("value_counts", valueCounts));
    }

    private CategorySummary summaryOf(WeeklyReportResponse response, String categoryCode) {
        return response.categorySummaries().stream()
                .filter(summary -> summary.category().equals(categoryCode))
                .findFirst()
                .orElseThrow();
    }
}
