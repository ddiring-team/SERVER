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
    DUPLICATE_ORDER_NUM("질문 순서(orderNum)가 중복되었습니다.");

    private final String message;
}
