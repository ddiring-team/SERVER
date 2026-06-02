package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyRepository;
import com.ddiring.ddiring_server.domain.family.exception.FamilyNotFoundException;
import com.ddiring.ddiring_server.domain.survey.domain.entity.Survey;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestion;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestionOption;
import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.QuestionType;
import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.SurveyStatus;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyQuestionOptionRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyQuestionRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.domain.survey.exception.DuplicateOrderNumException;
import com.ddiring.ddiring_server.domain.survey.exception.InvalidQuestionOptionsException;
import com.ddiring.ddiring_server.domain.survey.exception.NotGuardianException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyFamilyNotFoundException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyAlreadyActiveException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyNotFoundException;
import com.ddiring.ddiring_server.domain.survey.exception.SurveyNotOwnedException;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.CreateSurveyQuestionRequest;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.request.CreateSurveyRequest;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.CreateSurveyResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.SurveyListItemResponse;
import com.ddiring.ddiring_server.domain.survey.presentation.dto.response.TodaySurveyResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import com.ddiring.ddiring_server.global.notification.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private static final List<String> YES_NO_LABELS = List.of("예", "아니요");

    private final SurveyRepository surveyRepository;
    private final SurveyQuestionRepository surveyQuestionRepository;
    private final SurveyQuestionOptionRepository surveyQuestionOptionRepository;
    private final SurveySessionRepository surveySessionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;
    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyRepository familyRepository;
    private final FcmService fcmService;

    @Transactional
    public CreateSurveyResponse createSurvey(Long userId, CreateSurveyRequest request) {
        User guardian = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (guardian.getRole() != Role.GUARDIAN) {
            throw new NotGuardianException();
        }

        validateQuestions(request.questions());

        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(SurveyFamilyNotFoundException::new);
        Family family = familyRepository.findById(familyId)
                .orElseThrow(FamilyNotFoundException::new);

        Survey survey = surveyRepository.save(
                Survey.builder()
                        .family(family)
                        .createdBy(guardian)
                        .title(request.title())
                        .build()
        );

        for (CreateSurveyQuestionRequest q : request.questions()) {
            SurveyQuestion question = surveyQuestionRepository.save(
                    SurveyQuestion.builder()
                            .survey(survey)
                            .category(q.category() == null ? null : q.category().getDisplayName())
                            .content(q.content())
                            .questionType(q.questionType())
                            .orderNum(q.orderNum())
                            .build()
            );
            saveQuestionOptions(question, q);
        }

        return CreateSurveyResponse.of(survey.getId(), survey.getTitle(), request.questions().size());
    }

    @Transactional(readOnly = true)
    public List<SurveyListItemResponse> getSurveyList(Long userId) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(SurveyFamilyNotFoundException::new);

        List<Survey> surveys = surveyRepository.findAllByFamily_IdOrderByCreatedAtDesc(familyId);
        if (surveys.isEmpty()) {
            return List.of();
        }

        List<Long> surveyIds = surveys.stream().map(Survey::getId).toList();
        Map<Long, Integer> questionCountMap = toCountMap(
                surveyQuestionRepository.countGroupedBySurveyIds(surveyIds));
        Map<Long, Integer> responseCountMap = toCountMap(
                surveySessionRepository.countGroupedBySurveyIdsAndStatus(surveyIds, SurveyStatus.COMPLETED));

        return surveys.stream()
                .map(survey -> SurveyListItemResponse.of(
                        survey,
                        questionCountMap.getOrDefault(survey.getId(), 0),
                        responseCountMap.getOrDefault(survey.getId(), 0)
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TodaySurveyResponse> getTodaySurvey(Long userId) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(SurveyFamilyNotFoundException::new);

        List<Survey> activeSurveys = surveyRepository.findActiveByFamilyId(familyId);
        if (activeSurveys.isEmpty()) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        for (Survey survey : activeSurveys) {
            boolean alreadyDone = surveySessionRepository.existsCompletedByElderAndSurveyAndDate(userId, survey.getId(), today);
            if (!alreadyDone) {
                return Optional.of(TodaySurveyResponse.of(survey));
            }
        }

        return Optional.empty();
    }

    @Transactional
    public void activateSurvey(Long userId, Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(SurveyNotFoundException::new);

        validateOwner(userId, survey);

        if (survey.isActive()) {
            throw new SurveyAlreadyActiveException();
        }
        survey.activate();

        List<String> elderTokens = familyMemberRepository.findElderFcmTokensByFamilyId(survey.getFamily().getId());
        fcmService.sendToTokens(elderTokens, "새 설문이 도착했어요!", "'" + survey.getTitle() + "' 설문에 참여해 주세요.");
    }

    @Transactional
    public void deleteSurvey(Long userId, Long surveyId) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(SurveyNotFoundException::new);

        validateOwner(userId, survey);

        // 자식 데이터부터 명시적으로 삭제: SurveyAnswer → SurveySession → SurveyQuestionOption → SurveyQuestion → Survey
        List<Long> sessionIds = surveySessionRepository.findIdsBySurveyId(surveyId);
        if (!sessionIds.isEmpty()) {
            surveyAnswerRepository.deleteAllBySessionIdIn(sessionIds);
            surveySessionRepository.deleteAllBySurveyId(surveyId);
        }

        List<Long> questionIds = surveyQuestionRepository.findIdsBySurveyId(surveyId);
        if (!questionIds.isEmpty()) {
            surveyQuestionOptionRepository.deleteAllByQuestionIdIn(questionIds);
            surveyQuestionRepository.deleteAllBySurveyId(surveyId);
        }

        surveyRepository.delete(survey);
    }

    private void validateOwner(Long userId, Survey survey) {
        if (!survey.getCreatedBy().getId().equals(userId)) {
            throw new SurveyNotOwnedException();
        }
    }

    private void validateQuestions(List<CreateSurveyQuestionRequest> questions) {
        Set<Integer> orderNums = new HashSet<>();
        for (CreateSurveyQuestionRequest q : questions) {
            if (!orderNums.add(q.orderNum())) {
                throw new DuplicateOrderNumException();
            }
            validateOptionsByType(q);
        }
    }

    private void validateOptionsByType(CreateSurveyQuestionRequest q) {
        int optionSize = q.options() == null ? 0 : q.options().size();
        switch (q.questionType()) {
            case SCALE, MULTIPLE -> {
                if (optionSize < 2) {
                    throw new InvalidQuestionOptionsException();
                }
            }
            case YES_NO, TEXT -> {
                if (optionSize > 0) {
                    throw new InvalidQuestionOptionsException();
                }
            }
        }
    }

    private void saveQuestionOptions(SurveyQuestion question, CreateSurveyQuestionRequest q) {
        List<String> labels = resolveOptionLabels(q);
        for (int i = 0; i < labels.size(); i++) {
            surveyQuestionOptionRepository.save(
                    SurveyQuestionOption.builder()
                            .question(question)
                            .label(labels.get(i))
                            .orderNum(i + 1)
                            .build()
            );
        }
    }

    // YES_NO는 서버가 자동으로 [예, 아니요] 옵션 생성, SCALE/MULTIPLE은 요청 값 사용, TEXT는 옵션 없음
    private List<String> resolveOptionLabels(CreateSurveyQuestionRequest q) {
        return switch (q.questionType()) {
            case YES_NO -> YES_NO_LABELS;
            case SCALE, MULTIPLE -> q.options();
            case TEXT -> List.of();
        };
    }

    private Map<Long, Integer> toCountMap(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> ((Number) row[1]).intValue()
        ));
    }
}
