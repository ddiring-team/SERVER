package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.global.client.fastapi.FastApiSurveyClient;
import com.ddiring.ddiring_server.global.client.fastapi.dto.DailySummaryRequest;
import com.ddiring.ddiring_server.global.client.fastapi.dto.DailySummaryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailySummaryService {

    private final SurveySessionRepository sessionRepository;
    private final SurveyAnswerRepository answerRepository;
    private final FastApiSurveyClient fastApiSurveyClient;
    private final ObjectMapper objectMapper;
    private final DailySummaryPersistenceService persistenceService;

    @Async
    public void generateAndSave(Long sessionId) {
        // 1단계: DB 조회 — 별도 빈 호출로 @Transactional 정상 적용
        DailySummaryRequest request = persistenceService.buildRequest(sessionId);
        if (request == null) return;

        // 2단계: 외부 API 호출 (트랜잭션 밖)
        Optional<DailySummaryResponse> result = fastApiSurveyClient.getDailySummary(request);
        if (result.isEmpty()) {
            log.warn("일일 요약 생성 실패: 세션 {}", sessionId);
            return;
        }

        // 3단계: 결과 저장 — 별도 빈 호출로 @Transactional 정상 적용
        DailySummaryResponse response = result.get();
        persistenceService.saveSummary(sessionId, response.summary(), toJson(response.highlights()));
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("highlights JSON 직렬화 실패: {}", e.getMessage());
            return "[]";
        }
    }

    // --- 트랜잭션 분리를 위한 내부 퍼시스턴스 빈 ---

    @Service
    @RequiredArgsConstructor
    public static class DailySummaryPersistenceService {

        private final SurveySessionRepository sessionRepository;
        private final SurveyAnswerRepository answerRepository;

        @Transactional(readOnly = true)
        public DailySummaryRequest buildRequest(Long sessionId) {
            SurveySession session = sessionRepository.findById(sessionId).orElse(null);
            if (session == null) return null;

            Map<String, String> responses = buildResponsesMap(sessionId);
            if (responses.isEmpty()) {
                return null;
            }

            return new DailySummaryRequest(
                    new DailySummaryRequest.ElderProfile(session.getElder().getName()),
                    new DailySummaryRequest.DailyResponse(session.getSessionDate().toString(), responses)
            );
        }

        @Transactional
        public void saveSummary(Long sessionId, String summary, String highlightsJson) {
            sessionRepository.findById(sessionId).ifPresent(session ->
                    session.updateDailySummary(summary, highlightsJson)
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
}
