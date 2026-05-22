package com.ddiring.ddiring_server.domain.survey.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    SURVEY_CREATE_SUCCESS("설문이 생성되었습니다."),
    SURVEY_LIST_SUCCESS("설문 목록 조회에 성공했습니다."),
    SURVEY_STATUS_TOGGLE_SUCCESS("설문 상태가 변경되었습니다."),
    SURVEY_DELETE_SUCCESS("설문이 삭제되었습니다.");

    private final String message;
}
