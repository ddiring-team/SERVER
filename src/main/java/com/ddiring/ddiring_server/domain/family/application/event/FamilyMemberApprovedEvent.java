package com.ddiring.ddiring_server.domain.family.application.event;

/**
 * 가족 구성원 가입이 승인되었을 때 발행되는 도메인 이벤트.
 * LAZY 연관 접근으로 인한 {@code LazyInitializationException}을 피하기 위해
 * 알림에 필요한 값은 트랜잭션 범위 내에서 미리 추출해 담는다.
 */
public record FamilyMemberApprovedEvent(String fcmToken, String familyName) {
}
