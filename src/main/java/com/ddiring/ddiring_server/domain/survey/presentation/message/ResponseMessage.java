package com.ddiring.ddiring_server.domain.survey.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    SURVEY_CREATE_SUCCESS("설문이 생성되었습니다."),
    SURVEY_LIST_SUCCESS("설문 목록 조회에 성공했습니다."),
    SURVEY_STATUS_TOGGLE_SUCCESS("설문 상태가 변경되었습니다."),
    SURVEY_DELETE_SUCCESS("설문이 삭제되었습니다."),
    SURVEY_SESSION_START_SUCCESS("설문이 시작되었습니다."),
    SURVEY_ANSWER_SUBMIT_SUCCESS("답변이 제출되었습니다."),
    TODAY_SURVEY_SUCCESS("오늘의 설문 조회에 성공했습니다."),
    TODAY_SURVEY_NONE("오늘 진행할 설문이 없습니다."),
    SESSION_LIST_SUCCESS("설문 응답 목록 조회에 성공했습니다."),
    SESSION_DETAIL_SUCCESS("설문 응답 상세 조회에 성공했습니다.");

    private final String message;
}
