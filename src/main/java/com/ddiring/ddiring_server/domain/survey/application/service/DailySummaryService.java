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

    @Async
    public void generateAndSave(Long sessionId) {
        // 1단계: DB 조회 (트랜잭션 종료 후 커넥션 반환)
        DailySummaryRequest request = buildRequest(sessionId);
        if (request == null) return;

        // 2단계: 외부 API 호출 (트랜잭션 밖)
        Optional<DailySummaryResponse> result = fastApiSurveyClient.getDailySummary(request);
        if (result.isEmpty()) {
            log.warn("일일 요약 생성 실패: 세션 {}", sessionId);
            return;
        }

        // 3단계: 결과 저장 (별도 트랜잭션)
        DailySummaryResponse response = result.get();
        saveSummary(sessionId, response.summary(), toJson(response.highlights()));
    }

    @Transactional(readOnly = true)
    public DailySummaryRequest buildRequest(Long sessionId) {
        SurveySession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) return null;

        Map<String, String> responses = buildResponsesMap(sessionId);
        if (responses.isEmpty()) {
            log.warn("일일 요약 생성 건너뜀: 세션 {} 에 카테고리 답변 없음", sessionId);
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
            String category = (String) row[0];
            String answer = (String) row[1];
            map.putIfAbsent(category, answer);
        }
        return map;
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.warn("highlights JSON 직렬화 실패: {}", e.getMessage());
            return "[]";
        }
    }
}
