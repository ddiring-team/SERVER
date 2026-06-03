package com.ddiring.ddiring_server.domain.family.application.event;

import com.ddiring.ddiring_server.global.notification.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 가입 승인 알림을 트랜잭션 커밋 이후에 발송한다.
 * 외부 API(FCM) 호출을 트랜잭션 밖으로 분리해 커넥션 점유와 롤백 시 일관성 문제를 방지한다.
 */
@Component
@RequiredArgsConstructor
public class FamilyMemberApprovedEventListener {

    private static final String APPROVAL_ALERT_TITLE = "가족 가입 승인";

    private final FcmService fcmService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FamilyMemberApprovedEvent event) {
        String body = "'" + event.familyName() + "' 가족 가입이 승인되었어요.";
        fcmService.sendToTokens(List.of(event.fcmToken()), APPROVAL_ALERT_TITLE, body);
    }
}
