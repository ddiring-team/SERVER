package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.survey.application.dto.TransformedQuestionData;
import com.ddiring.ddiring_server.domain.survey.application.service.SurveySessionStarter.PreparedSession;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestion;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestionOption;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyAlreadyCompletedException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveySessionNotFoundException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveySessionNotOwnedException;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.ElderSessionListItemResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.ElderTodaySurveyResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SessionDetailResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.StartSurveySessionResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SurveyOptionResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SurveyQuestionForSessionResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import com.ddiring.ddiring_server.global.client.fastapi.dto.TransformQuestionsResponse;
import com.ddiring.ddiring_server.global.notification.FcmService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveySessionService {

    private final SurveySessionStarter starter;
    private final SurveyQuestionTransformService transformService;
    private final TransformedQuestionCacheService cacheService;
    private final SurveySessionRepository sessionRepository;
    private final SurveyAnswerRepository answerRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    private static final String SURVEY_REMINDER_TITLE = "설문 독려 알림";

    public StartSurveySessionResponse startSession(Long userId, Long surveyId) {
        PreparedSession prepared = starter.prepare(userId, surveyId);

        Map<String, TransformedQuestionData> dataByKey = callTransformWithCache(prepared);

        List<SurveyQuestionForSessionResponse> questions = prepared.questions().stream()
                .map(q -> toQuestionResponse(q, prepared.optionsByQuestionId(), dataByKey))
                .toList();

        return new StartSurveySessionResponse(
                prepared.sessionId(),
                prepared.surveyId(),
                prepared.surveyTitle(),
                questions
        );
    }

    private Map<String, TransformedQuestionData> callTransformWithCache(PreparedSession prepared) {
        if (prepared.questions().isEmpty()) {
            return Map.of();
        }

        List<String> allKeys = prepared.questions().stream()
                .map(q -> String.valueOf(q.getId()))
                .toList();

        LocalDate today = LocalDate.now();
        Map<String, TransformedQuestionData> cached = cacheService.findCached(prepared.elderId(), today, allKeys);

        List<SurveyQuestion> uncachedQuestions = prepared.questions().stream()
                .filter(q -> !cached.containsKey(String.valueOf(q.getId())))
                .toList();

        Map<String, TransformedQuestionData> fromApi = Map.of();
        if (!uncachedQuestions.isEmpty()) {
            Optional<TransformQuestionsResponse> response = transformService.transform(
                    prepared.elderName(), prepared.elderId(), uncachedQuestions);

            fromApi = response
                    .map(r -> r.transformedQuestions().stream()
                            .filter(t -> t.transformed() != null && !t.transformed().isBlank())
                            .collect(Collectors.toMap(
                                    TransformQuestionsResponse.TransformedQuestion::key,
                                    t -> new TransformedQuestionData(t.transformed(), t.audioUrl()),
                                    (existing, duplicate) -> existing
                            )))
                    .orElseGet(Map::of);

            if (!fromApi.isEmpty()) {
                cacheService.saveAll(prepared.elderId(), today, fromApi);
            }
        }

        Map<String, TransformedQuestionData> merged = new HashMap<>(cached);
        merged.putAll(fromApi);
        return merged;
    }

    @Transactional(readOnly = true)
    public List<ElderSessionListItemResponse> getElderSessionList(Long requesterId, Long elderId) {
        verifyInSameFamily(requesterId, elderId);
        return sessionRepository.findCompletedByElderIdOrderByDateDesc(elderId).stream()
                .map(ElderSessionListItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ElderTodaySurveyResponse getElderTodaySurveyStatus(Long requesterId, Long elderId) {
        verifyInSameFamily(requesterId, elderId);
        boolean completed = sessionRepository.existsCompletedByElderIdAndDate(elderId, LocalDate.now());
        return ElderTodaySurveyResponse.of(completed);
    }

    /**
     * 보호자가 아직 오늘 설문을 완료하지 않은 어르신에게 설문 독려 푸시를 보낸다.
     * 어르신이 오늘 이미 완료했으면 보낼 필요가 없어 예외로 막는다.
     */
    @Transactional(readOnly = true)
    public void sendSurveyReminder(Long requesterId, Long elderId) {
        verifyInSameFamily(requesterId, elderId);

        if (sessionRepository.existsCompletedByElderIdAndDate(elderId, LocalDate.now())) {
            throw new SurveyAlreadyCompletedException();
        }

        User elder = userRepository.findById(elderId)
                .orElseThrow(UserNotFoundException::new);

        if (elder.getFcmToken() == null) {
            return;
        }
        String elderName = elder.getName() != null ? elder.getName() : "어르신";
        String message = elderName + "님, 오늘의 안부 설문이 아직 남아있어요. 잊지 말고 응답해 주세요!";
        fcmService.sendToTokens(List.of(elder.getFcmToken()), SURVEY_REMINDER_TITLE, message);
    }

    @Transactional(readOnly = true)
    public SessionDetailResponse getSessionDetail(Long requesterId, Long sessionId) {
        SurveySession session = sessionRepository.findById(sessionId)
                .orElseThrow(SurveySessionNotFoundException::new);

        verifyInSameFamily(requesterId, session.getElder().getId());

        List<SessionDetailResponse.AnswerItem> answers = buildAnswerItems(sessionId);
        List<String> highlights = parseHighlights(session.getDailyHighlights());

        return new SessionDetailResponse(
                session.getId(),
                session.getSurvey().getTitle(),
                session.getSessionDate(),
                session.getCompletedAt(),
                answers,
                session.getDailySummary(),
                highlights
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

    private List<SessionDetailResponse.AnswerItem> buildAnswerItems(Long sessionId) {
        List<Object[]> rows = answerRepository.findAnswerDetailsBySessionId(sessionId);
        // MULTIPLE 타입은 같은 카테고리+질문에 여러 행 → 답변을 ", "로 합산
        Map<String, SessionDetailResponse.AnswerItem> merged = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            String question = (String) row[1];
            String answer = (String) row[2];
            String key = category + "|" + question;
            if (merged.containsKey(key)) {
                SessionDetailResponse.AnswerItem existing = merged.get(key);
                merged.put(key, new SessionDetailResponse.AnswerItem(
                        existing.category(), existing.question(), existing.answer() + ", " + answer));
            } else {
                merged.put(key, new SessionDetailResponse.AnswerItem(category, question, answer));
            }
        }
        return new ArrayList<>(merged.values());
    }

    private List<String> parseHighlights(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("highlights JSON 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private SurveyQuestionForSessionResponse toQuestionResponse(
            SurveyQuestion q,
            Map<Long, List<SurveyQuestionOption>> optionsByQuestionId,
            Map<String, TransformedQuestionData> dataByKey
    ) {
        String key = String.valueOf(q.getId());
        TransformedQuestionData data = dataByKey.get(key);
        String displayContent = (data != null) ? data.transformed() : q.getContent();
        String audioUrl = (data != null) ? data.audioUrl() : null;

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
                audioUrl,
                options
        );
    }
}
