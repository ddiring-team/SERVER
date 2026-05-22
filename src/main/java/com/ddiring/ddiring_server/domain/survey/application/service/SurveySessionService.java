package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.survey.application.service.SurveySessionStarter.PreparedSession;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestion;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestionOption;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.StartSurveySessionResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SurveyOptionResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SurveyQuestionForSessionResponse;
import com.ddiring.ddiring_server.global.client.fastapi.dto.TransformQuestionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 어르신의 설문 세션 시작 API 진입점.
 * - DB 작업은 SurveySessionStarter(@Transactional)에 위임
 * - 캐시 조회/저장은 TransformedQuestionCacheService에 위임
 * - FastAPI 호출은 SurveyQuestionTransformService(트랜잭션 밖)에 위임
 * - 변환 실패 시 원본 질문으로 fallback
 */
@Service
@RequiredArgsConstructor
public class SurveySessionService {

    private final SurveySessionStarter starter;
    private final SurveyQuestionTransformService transformService;
    private final TransformedQuestionCacheService cacheService;

    public StartSurveySessionResponse startSession(Long userId, Long surveyId) {
        PreparedSession prepared = starter.prepare(userId, surveyId);

        Map<String, String> transformedByKey = callTransformWithCache(prepared);

        List<SurveyQuestionForSessionResponse> questions = prepared.questions().stream()
                .map(q -> toQuestionResponse(q, prepared.optionsByQuestionId(), transformedByKey))
                .toList();

        return new StartSurveySessionResponse(
                prepared.sessionId(),
                prepared.surveyId(),
                prepared.surveyTitle(),
                questions
        );
    }

    private Map<String, String> callTransformWithCache(PreparedSession prepared) {
        if (prepared.questions().isEmpty()) {
            return Map.of();
        }

        List<String> allKeys = prepared.questions().stream()
                .map(q -> String.valueOf(q.getId()))
                .toList();

        LocalDate today = LocalDate.now();
        Map<String, String> cached = cacheService.findCached(prepared.elderId(), today, allKeys);

        List<SurveyQuestion> uncachedQuestions = prepared.questions().stream()
                .filter(q -> !cached.containsKey(String.valueOf(q.getId())))
                .toList();

        Map<String, String> fromApi = Map.of();
        if (!uncachedQuestions.isEmpty()) {
            Optional<TransformQuestionsResponse> response = transformService.transform(
                    prepared.elderName(), prepared.elderId(), uncachedQuestions);

            fromApi = response
                    .map(r -> r.transformedQuestions().stream()
                            .filter(t -> t.transformed() != null && !t.transformed().isBlank())
                            .collect(Collectors.toMap(
                                    TransformQuestionsResponse.TransformedQuestion::key,
                                    TransformQuestionsResponse.TransformedQuestion::transformed,
                                    (existing, duplicate) -> existing
                            )))
                    .orElseGet(Map::of);

            if (!fromApi.isEmpty()) {
                cacheService.saveAll(prepared.elderId(), today, fromApi);
            }
        }

        Map<String, String> merged = new HashMap<>(cached);
        merged.putAll(fromApi);
        return merged;
    }

    private SurveyQuestionForSessionResponse toQuestionResponse(
            SurveyQuestion q,
            Map<Long, List<SurveyQuestionOption>> optionsByQuestionId,
            Map<String, String> transformedByKey
    ) {
        String key = String.valueOf(q.getId());
        String displayContent = transformedByKey.getOrDefault(key, q.getContent());

        List<SurveyOptionResponse> options = optionsByQuestionId
                .getOrDefault(q.getId(), List.of())
                .stream()
                .map(SurveyOptionResponse::from)
                .toList();

        return new SurveyQuestionForSessionResponse(
                q.getId(),
                q.getOrderNum(),
                q.getCategory(),
                q.getQuestionType(),
                q.getContent(),
                displayContent,
                options
        );
    }
}
