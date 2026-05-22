package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestion;
import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveyAnswerRepository;
import com.ddiring.ddiring_server.domain.survey.domain.repository.SurveySessionRepository;
import com.ddiring.ddiring_server.global.client.fastapi.dto.TransformQuestionsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FastAPI 질문 변환 요청 DTO를 DB에서 조립하는 책임만 갖는다.
 * 외부 HTTP 호출은 호출부에서 분리해 수행하고, 본 컴포넌트는 트랜잭션 내부에서 DB 조회만 끝낸다.
 */
@Component
@RequiredArgsConstructor
public class SurveyTransformRequestAssembler {

    private static final int RECENT_RESPONSE_DAYS = 3;

    private final SurveySessionRepository surveySessionRepository;
    private final SurveyAnswerRepository surveyAnswerRepository;

    @Transactional(readOnly = true)
    public TransformQuestionsRequest assemble(
            String elderName,
            Long elderId,
            List<SurveyQuestion> questions
    ) {
        List<TransformQuestionsRequest.QuestionItem> questionItems = questions.stream()
                .map(q -> new TransformQuestionsRequest.QuestionItem(
                        String.valueOf(q.getId()),
                        q.getContent(),
                        q.getQuestionType().toResponseType()
                ))
                .toList();

        return new TransformQuestionsRequest(
                new TransformQuestionsRequest.ElderProfile(elderName),
                questionItems,
                buildRecentResponses(elderId)
        );
    }

    // 최근 3일(오늘 포함) 완료된 세션의 카테고리별 답변을 수집한다.
    private List<TransformQuestionsRequest.RecentResponse> buildRecentResponses(Long elderId) {
        LocalDate since = LocalDate.now().minusDays(RECENT_RESPONSE_DAYS - 1L);
        List<SurveySession> recentSessions = surveySessionRepository.findRecentCompletedByElderId(elderId, since);

        return recentSessions.stream()
                .map(session -> {
                    List<Object[]> rows = surveyAnswerRepository.findCategoryAnswersBySessionId(session.getId());
                    // 같은 세션 내 동일 카테고리는 orderNum이 큰(나중) 답변이 우선
                    Map<String, String> responses = rows.stream()
                            .filter(row -> row[0] != null && row[1] != null)
                            .collect(Collectors.toMap(
                                    row -> (String) row[0],
                                    row -> (String) row[1],
                                    (existing, duplicate) -> duplicate
                            ));
                    return new TransformQuestionsRequest.RecentResponse(
                            session.getSessionDate().toString(),
                            responses
                    );
                })
                .toList();
    }
}
