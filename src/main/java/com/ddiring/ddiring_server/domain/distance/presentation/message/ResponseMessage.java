package com.ddiring.ddiring_server.domain.distance.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    DISTANCE_LIST_SUCCESS("안부거리 목록을 조회했습니다.");

    private final String message;
}
