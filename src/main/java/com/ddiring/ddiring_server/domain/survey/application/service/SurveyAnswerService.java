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
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.SubmitAnswerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .filter(a -> a.selectedOptionId() != null)
                .map(SubmitAnswerRequest::selectedOptionId)
                .toList();

        Map<Long, SurveyQuestionOption> optionMap = optionIds.isEmpty()
                ? Map.of()
                : optionRepository.findAllById(optionIds).stream()
                        .collect(Collectors.toMap(SurveyQuestionOption::getId, Function.identity()));

        List<SurveyAnswer> entities = answers.stream()
                .map(req -> buildAnswer(req, session, questionMap, optionMap))
                .toList();

        answerRepository.saveAll(entities);
        session.complete();
    }

    private SurveyAnswer buildAnswer(
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
            return SurveyAnswer.builder()
                    .session(session)
                    .question(question)
                    .answerText(req.answerText())
                    .build();
        }

        // YES_NO / SCALE / MULTIPLE
        if (req.selectedOptionId() == null) {
            throw new InvalidAnswerFormatException();
        }
        SurveyQuestionOption option = optionMap.get(req.selectedOptionId());
        if (option == null || !option.getQuestion().getId().equals(question.getId())) {
            throw new InvalidAnswerFormatException();
        }
        return SurveyAnswer.builder()
                .session(session)
                .question(question)
                .selectedOption(option)
                .build();
    }
}
