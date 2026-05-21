package com.ddiring.ddiring_server.global.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FcmService {

    public void sendToTokens(List<String> tokens, String title, String body) {
        if (tokens.isEmpty()) {
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase 미초기화 상태 — FCM 전송 생략");
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            var response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("FCM 전송 완료 — 성공: {}, 실패: {}", response.getSuccessCount(), response.getFailureCount());
        } catch (Exception e) {
            log.warn("FCM 전송 실패: {}", e.getMessage());
        }
    }
}
