package com.ddiring.ddiring_server.domain.family.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    FAMILY_CREATE_SUCCESS("가족방이 생성되었습니다."),
    MEMBER_LIST_SUCCESS("구성원 목록을 조회했습니다."),
    ELDER_LIST_SUCCESS("연결된 어르신 목록을 조회했습니다."),
    MEMBER_APPROVE_SUCCESS("구성원을 승인했습니다."),
    MEMBER_REJECT_SUCCESS("구성원을 거절했습니다."),
    FAMILY_JOIN_SUCCESS("가족방 입장 요청이 완료되었습니다."),
    FAMILY_STATUS_SUCCESS("가족방 가입 상태를 조회했습니다."),
    INVITE_CODE_SUCCESS("초대코드를 조회했습니다.");

    private final String message;
}
