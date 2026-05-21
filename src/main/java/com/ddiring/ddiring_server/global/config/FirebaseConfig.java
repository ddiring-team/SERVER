package com.ddiring.ddiring_server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path:firebase-service-account.json}")
    private String serviceAccountPath;

    // 배포 환경: 환경변수 FIREBASE_SERVICE_ACCOUNT_JSON 에 JSON 내용을 직접 넣어서 사용
    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String serviceAccountJson;

    @PostConstruct
    public void initialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try {
            InputStream stream = resolveCredentialStream();
            if (stream == null) {
                log.warn("Firebase 서비스 계정 정보를 찾을 수 없습니다. FCM 기능이 비활성화됩니다.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(stream))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase 초기화 완료");
        } catch (IOException e) {
            log.warn("Firebase 초기화 실패: {}. FCM 기능이 비활성화됩니다.", e.getMessage());
        }
    }

    private InputStream resolveCredentialStream() throws IOException {
        // 환경변수 우선 (배포 환경)
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }
        // 파일 폴백 (로컬 개발) — ClassPathResource로 classpath: 접두사 처리
        ClassPathResource resource = new ClassPathResource(serviceAccountPath);
        return resource.exists() ? resource.getInputStream() : null;
    }
}
