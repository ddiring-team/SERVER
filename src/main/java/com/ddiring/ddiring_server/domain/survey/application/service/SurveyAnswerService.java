package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyAnswer;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestion;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestionOption;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.QuestionType;
import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.SurveyStatus;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyQuestionOptionRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyQuestionRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.domain.survey.exception.InvalidAnswerFormatException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyAlreadyCompletedException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveySessionNotFoundException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveySessionNotOwnedException;
import com.ddiring.ddiring_server.domain.distance.application.event.DistanceResetEvent;
import com.ddiring.ddiring_server.domain.distance.domain.entity.enums.DistanceActionType;
import com.ddiring.ddiring_server.domain.temperature.application.event.TemperatureRaiseEvent;
import com.ddiring.ddiring_server.domain.temperature.domain.entity.enums.TemperatureActionType;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.SubmitAnswerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyAnswerService {

    private final SurveySessionRepository sessionRepository;
    private final SurveyQuestionRepository questionRepository;
    private final SurveyQuestionOptionRepository optionRepository;
    private final SurveyAnswerRepository answerRepository;
    private final DailySummaryService dailySummaryService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void submitAnswers(Long userId, Long sessionId, List<SubmitAnswerRequest> answers) {
        SurveySession session = sessionRepository.findById(sessionId)
                .orElseThrow(SurveySessionNotFoundException::new);

        if (!session.getElder().getId().equals(userId)) {
            throw new SurveySessionNotOwnedException();
        }
        if (session.getStatus() == SurveyStatus.COMPLETED) {
            throw new SurveyAlreadyCompletedException();
        }

        List<Long> questionIds = answers.stream()
                .map(SubmitAnswerRequest::questionId)
                .toList();

        Map<Long, SurveyQuestion> questionMap = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(SurveyQuestion::getId, Function.identity()));

        List<Long> optionIds = answers.stream()
                .filter(a -> a.selectedOptionIds() != null)
                .flatMap(a -> a.selectedOptionIds().stream())
                .toList();

        Map<Long, SurveyQuestionOption> optionMap = optionIds.isEmpty()
                ? Map.of()
                : optionRepository.findAllById(optionIds).stream()
                        .collect(Collectors.toMap(SurveyQuestionOption::getId, Function.identity()));

        List<SurveyAnswer> entities = answers.stream()
                .flatMap(req -> buildAnswers(req, session, questionMap, optionMap).stream())
                .toList();

        answerRepository.saveAll(entities);
        session.complete();

        eventPublisher.publishEvent(DistanceResetEvent.broadcast(userId, DistanceActionType.SURVEY_ANSWER));
        eventPublisher.publishEvent(new TemperatureRaiseEvent(userId, TemperatureActionType.SURVEY_ANSWER));

        // 부모 트랜잭션 커밋 완료 후 비동기 실행 — 커밋 전 실행 시 답변 데이터 미조회 방지
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                dailySummaryService.generateAndSave(sessionId);
            }
        });
    }

    private List<SurveyAnswer> buildAnswers(
            SubmitAnswerRequest req,
            SurveySession session,
            Map<Long, SurveyQuestion> questionMap,
            Map<Long, SurveyQuestionOption> optionMap
    ) {
        SurveyQuestion question = questionMap.get(req.questionId());
        if (question == null) {
            throw new InvalidAnswerFormatException();
        }

        if (question.getQuestionType() == QuestionType.TEXT) {
            if (req.answerText() == null || req.answerText().isBlank()) {
                throw new InvalidAnswerFormatException();
            }
            return List.of(SurveyAnswer.builder()
                    .session(session)
                    .question(question)
                    .answerText(req.answerText())
                    .build());
        }

        if (question.getQuestionType() == QuestionType.MULTIPLE) {
            List<Long> ids = req.selectedOptionIds();
            if (ids == null || ids.isEmpty()) {
                throw new InvalidAnswerFormatException();
            }
            return ids.stream()
                    .map(id -> {
                        SurveyQuestionOption option = optionMap.get(id);
                        if (option == null || !option.getQuestion().getId().equals(question.getId())) {
                            throw new InvalidAnswerFormatException();
                        }
                        return SurveyAnswer.builder()
                                .session(session)
                                .question(question)
                                .selectedOption(option)
                                .build();
                    })
                    .toList();
        }

        // YES_NO / SCALE
        List<Long> ids = req.selectedOptionIds();
        if (ids == null || ids.size() != 1) {
            throw new InvalidAnswerFormatException();
        }
        SurveyQuestionOption option = optionMap.get(ids.get(0));
        if (option == null || !option.getQuestion().getId().equals(question.getId())) {
            throw new InvalidAnswerFormatException();
        }
        return List.of(SurveyAnswer.builder()
                .session(session)
                .question(question)
                .selectedOption(option)
                .build());
    }
}
