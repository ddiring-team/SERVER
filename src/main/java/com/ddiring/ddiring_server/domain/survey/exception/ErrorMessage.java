package com.ddiring.ddiring_server.domain.survey.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {

    NOT_GUARDIAN("보호자만 설문을 생성할 수 있습니다."),
    NOT_IN_FAMILY("가족방에 소속되어 있지 않습니다."),
    SURVEY_NOT_FOUND("설문을 찾을 수 없습니다."),
    SURVEY_NOT_OWNED("해당 설문에 대한 권한이 없습니다."),
    INVALID_QUESTION_OPTIONS("질문 유형에 맞지 않는 선택지 구성입니다."),
    DUPLICATE_ORDER_NUM("질문 순서(orderNum)가 중복되었습니다."),
    NOT_ELDER_ROLE("어르신만 설문에 응답할 수 있습니다."),
    SURVEY_NOT_ACTIVE("비활성화된 설문입니다."),
    SURVEY_NOT_IN_FAMILY("같은 가족방의 설문이 아닙니다."),
    SURVEY_ALREADY_COMPLETED("오늘 이미 응답을 완료한 설문입니다."),
    SURVEY_SESSION_NOT_FOUND("설문 세션을 찾을 수 없습니다."),
    SURVEY_SESSION_NOT_OWNED("해당 설문 세션에 대한 권한이 없습니다."),
    INVALID_ANSWER_FORMAT("질문 유형에 맞지 않는 답변 형식입니다.");

    private final String message;
}
