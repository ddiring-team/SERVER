package com.ddiring.ddiring_server.domain.survey.domain.entity.enums;

public enum QuestionType {
    YES_NO,   // 네 / 아니요
    SCALE,    // 3단계 상태 (괜찮아요 / 조금 불편 / 많이 힘듦)
    MULTIPLE, // 복수 선택 (아침 / 점심 / 저녁)
    TEXT      // 자유 텍스트 입력 (가족에게 하고 싶은 말)
}
