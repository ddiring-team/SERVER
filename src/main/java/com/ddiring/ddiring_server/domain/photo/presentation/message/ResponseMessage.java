package com.ddiring.ddiring_server.domain.photo.presentation.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMessage {

    DAILY_PHOTO_CREATE_SUCCESS("일상이 공유되었습니다."),
    DAILY_PHOTO_LIST_SUCCESS("일상 목록을 조회했습니다."),
    DAILY_PHOTO_FEED_SUCCESS("가족방 피드를 조회했습니다."),
    REACTION_TOGGLE_SUCCESS("이모지 반응이 처리되었습니다.");

    private final String message;
}