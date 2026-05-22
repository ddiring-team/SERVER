package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.survey.domain.entity.Survey;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestion;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestionOption;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.SurveyStatus;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyQuestionOptionRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyQuestionRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.domain.survey.exception.NotElderRoleException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyAlreadyCompletedException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyFamilyNotFoundException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyNotActiveException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyNotFoundException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyNotInFamilyException;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 세션 시작 시 검증/세션 생성/질문 fetch 까지의 트랜잭션 단위를 담당.
 * 외부(FastAPI) 호출은 호출부에서 트랜잭션 밖에서 처리한다.
 */
@Component
@RequiredArgsConstructor
public class SurveySessionStarter {

    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final SurveyRepository surveyRepository;
    private final SurveySessionRepository surveySessionRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;

    @Transactional
    public PreparedSession prepare(Long userId, Long surveyId) {
        User elder = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (elder.getRole() != Role.ELDER) {
            throw new NotElderRoleException();
        }

        Long elderFamilyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(SurveyFamilyNotFoundException::new);

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(SurveyNotFoundException::new);

        if (!survey.getFamily().getId().equals(elderFamilyId)) {
            throw new SurveyNotInFamilyException();
        }
        if (!survey.isActive()) {
            throw new SurveyNotActiveException();
        }

        LocalDate today = LocalDate.now();
        SurveySession session = surveySessionRepository
                .findByElder_IdAndSurvey_IdAndSessionDate(userId, surveyId, today)
                .map(existing -> {
                    if (existing.getStatus() == SurveyStatus.COMPLETED) {
                        throw new SurveyAlreadyCompletedException();
                    }
                    return existing;
                })
                .orElseGet(() -> surveySessionRepository.save(
                        SurveySession.builder()
                                .survey(survey)
                                .elder(elder)
                                .sessionDate(today)
                                .status(SurveyStatus.IN_PROGRESS)
                                .build()
                ));

        List<SurveyQuestion> questions = surveyQuestionRepository
                .findAllBySurvey_IdOrderByOrderNumAsc(surveyId);

        Map<Long, List<SurveyQuestionOption>> optionsByQuestionId = questions.isEmpty()
                ? Map.of()
                : surveyQuestionOptionRepository
                .findAllByQuestionIdInOrderByOrderNumAsc(questions.stream().map(SurveyQuestion::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(opt -> opt.getQuestion().getId()));

        return new PreparedSession(
                session.getId(),
                survey.getId(),
                survey.getTitle(),
                elder.getId(),
                elder.getName() != null ? elder.getName() : "어르신",
                questions,
                optionsByQuestionId
        );
    }

    public record PreparedSession(
            Long sessionId,
            Long surveyId,
            String surveyTitle,
            Long elderId,
            String elderName,
            List<SurveyQuestion> questions,
            Map<Long, List<SurveyQuestionOption>> optionsByQuestionId
    ) {}
}
