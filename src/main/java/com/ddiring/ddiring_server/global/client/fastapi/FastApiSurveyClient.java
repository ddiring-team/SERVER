package com.ddiring.ddiring_server.global.client.fastapi;

import com.ddiring.ddiring_server.global.client.fastapi.dto.TransformQuestionsRequest;
import com.ddiring.ddiring_server.global.client.fastapi.dto.TransformQuestionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiSurveyClient {

    private static final String TRANSFORM_QUESTIONS_URI = "/ai/transform-questions";
    private static final int MAX_ATTEMPTS = 3;          // 최초 1회 + 재시도 2회
    private static final long RETRY_BACKOFF_MS = 300L;

    private final RestClient fastApiRestClient;

    /**
     * FastAPI 질문 변환 호출 (동기).
     * 실패 시 빈 Optional 반환 → 호출자는 원본 질문으로 fallback 한다.
     */
    public Optional<TransformQuestionsResponse> transformQuestions(TransformQuestionsRequest request) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                TransformQuestionsResponse response = fastApiRestClient.post()
                        .uri(TRANSFORM_QUESTIONS_URI)
                        .body(request)
                        .retrieve()
                        .body(TransformQuestionsResponse.class);
                return Optional.ofNullable(response);

            } catch (RestClientResponseException e) {
                // 4xx은 재시도해도 동일 결과이므로 즉시 중단
                if (e.getStatusCode().is4xxClientError()) {
                    log.warn("FastAPI 질문 변환 실패 (status={}): {}", e.getStatusCode(), e.getMessage());
                    return Optional.empty();
                }
                if (attempt == MAX_ATTEMPTS) {
                    log.warn("FastAPI 질문 변환 실패 (최종 status={}): {}", e.getStatusCode(), e.getMessage());
                    return Optional.empty();
                }
                log.warn("FastAPI 질문 변환 재시도 {}/{} (status={})", attempt, MAX_ATTEMPTS, e.getStatusCode());

            } catch (Exception e) {
                // 네트워크/타임아웃 등 — 재시도 가능
                if (attempt == MAX_ATTEMPTS) {
                    log.warn("FastAPI 질문 변환 실패 (최종): {}", e.toString());
                    return Optional.empty();
                }
                log.warn("FastAPI 질문 변환 재시도 {}/{}: {}", attempt, MAX_ATTEMPTS, e.toString());
            }

            sleepBackoff(attempt);
        }
        return Optional.empty();
    }

    private void sleepBackoff(int attempt) {
        try {
            Thread.sleep(RETRY_BACKOFF_MS * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
