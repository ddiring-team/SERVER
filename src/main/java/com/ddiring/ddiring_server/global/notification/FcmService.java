package com.ddiring.ddiring_server.global.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
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

        for (String token : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(token)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();
                String response = FirebaseMessaging.getInstance().send(message);
                log.info("FCM 전송 성공: {}", response);
            } catch (Exception e) {
                log.warn("FCM 전송 실패 (token={}...): {}", token.substring(0, Math.min(10, token.length())), e.getMessage());
            }
        }
    }
}