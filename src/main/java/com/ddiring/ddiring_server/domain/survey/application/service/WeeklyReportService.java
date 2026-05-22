package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.domain.survey.exception.SurveySessionNotOwnedException;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.WeeklyReportResponse;
import com.ddiring.ddiring_server.global.client.fastapi.FastApiSurveyClient;
import com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
            return new WeeklyReportResponse(elderId, elderName, startDate, endDate, null, null, null);
        }

        com.ddiring.ddiring_server.global.client.fastapi.dto.WeeklyReportResponse fastApiResponse = result.get();
        return new WeeklyReportResponse(
                elderId,
                elderName,
                startDate,
                endDate,
                fastApiResponse.report(),
                fastApiResponse.patterns(),
                fastApiResponse.stats()
        );
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
