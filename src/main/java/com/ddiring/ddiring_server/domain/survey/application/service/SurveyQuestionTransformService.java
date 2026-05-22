package com.ddiring.ddiring_server.domain.survey.application.service;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestion;
import com.ddiring.ddiring_server.global.client.fastapi.FastApiSurveyClient;
import com.ddiring.ddiring_server.global.client.fastapi.dto.TransformQuestionsRequest;
import com.ddiring.ddiring_server.global.client.fastapi.dto.TransformQuestionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 어르신의 최근 응답 + 설문 질문을 FastAPI로 보내 개인화된 발화로 변환받는 도메인 서비스.
 * DB 조립은 트랜잭션 안(Assembler)에서 완료하고, HTTP 호출은 트랜잭션 밖에서 수행한다.
 * 외부 호출 실패 시 빈 Optional 반환 → 호출부에서 원본 질문으로 fallback.
 */
@Service
@RequiredArgsConstructor
public class SurveyQuestionTransformService {

    private final SurveyTransformRequestAssembler assembler;
    private final FastApiSurveyClient fastApiSurveyClient;

    public Optional<TransformQuestionsResponse> transform(
            String elderName,
            Long elderId,
            List<SurveyQuestion> questions
    ) {
        TransformQuestionsRequest request = assembler.assemble(elderName, elderId, questions);
        return fastApiSurveyClient.transformQuestions(request);
    }
}
