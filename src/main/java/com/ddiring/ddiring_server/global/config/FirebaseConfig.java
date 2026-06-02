package com.ddiring.ddiring_server.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    // 자격증명 위치. 배포: file:/app/firebase-service-account.json (볼륨 마운트), 로컬: classpath 기본값
    @Value("${firebase.service-account-path:classpath:firebase-service-account.json}")
    private String serviceAccountPath;

    // 폴백: 환경변수 FIREBASE_SERVICE_ACCOUNT_JSON 에 JSON 내용을 직접 넣어서 사용
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
        // 파일/클래스패스 리소스 우선 (file:, classpath: 접두사 모두 지원)
        Resource resource = resourceLoader.getResource(serviceAccountPath);
        if (resource.exists()) {
            return resource.getInputStream();
        }
        // 환경변수에 JSON 원문을 직접 넣은 경우 폴백
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }
}
