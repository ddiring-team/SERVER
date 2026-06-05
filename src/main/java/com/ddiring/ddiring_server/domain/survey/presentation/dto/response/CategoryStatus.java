package com.ddiring.ddiring_server.domain.survey.presentation.dto.response;

/**
 * 카테고리별 주간 상태. AI patterns(우려 패턴) + 응답 유무를 결합해 산출한다.
 */
public enum CategoryStatus {
    GOOD("양호"),              // 응답 충분 + 우려 패턴 없음
    INFO("참고"),              // info 패턴만 존재
    ATTENTION("주의 필요"),     // warning 이상 패턴 존재
    INSUFFICIENT("데이터 부족"), // 우려 패턴은 없으나 응답 일수가 부족해 상태 단정 불가
    NO_DATA("기록 없음");       // 해당 주 응답 자체가 없음

    private final String label;

    CategoryStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
