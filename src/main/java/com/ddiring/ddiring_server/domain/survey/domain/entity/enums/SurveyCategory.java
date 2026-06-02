package com.ddiring.ddiring_server.domain.survey.domain.entity.enums;

public enum SurveyCategory {
    MEDICATION("약 복용"),
    HEALTH("건강 상태"),
    MOOD("기분 / 감정"),
    MEAL("식사 / 수분"),
    ACTIVITY("활동 / 외출"),
    SAFETY("안전 / 생활"),
    COGNITION("인지 상태"),
    FALL_PREVENTION("낙상 예방"),
    FAMILY("가족 소통");

    // FastAPI 요청·응답 및 프론트 노출에 사용하는 표준 카테고리명.
    // SurveyQuestion.category(String)에는 이 값이 저장된다. (커스텀 질문은 null)
    private final String displayName;

    SurveyCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
