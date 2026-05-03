package com.ddiring.ddiring_server.global.storage.presentation;

import com.ddiring.ddiring_server.global.security.TokenProvider;
import com.ddiring.ddiring_server.global.storage.application.service.S3Service;
import com.ddiring.ddiring_server.global.storage.exception.InvalidMimeTypeException;
import com.ddiring.ddiring_server.global.storage.presentation.dto.request.PresignedUrlCreateRequest;
import com.ddiring.ddiring_server.global.storage.presentation.dto.response.PresignedUrlResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(S3Controller.class)
class S3ControllerTest {

    // WebSecurityConfig 가 로드되면서 JwtAuthenticationFilter → TokenProvider 의존성이 필요
    // 프로덕션 SecurityFilterChain 보다 높은 우선순위로 모든 요청을 허용하는 테스트용 체인을 등록
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Order(1)
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/**")
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("유효한 MIME 타입으로 요청하면 200 OK 와 Presigned URL 을 반환한다")
    @Test
    void getS3PresignedUrl_성공() throws Exception {
        // given
        PresignedUrlCreateRequest request = new PresignedUrlCreateRequest("image/jpeg");
        String fakeUrl = "https://test-bucket.s3.amazonaws.com/image/uuid.jpeg?X-Amz-Algorithm=AWS4";
        PresignedUrlResponse response = new PresignedUrlResponse(fakeUrl);

        given(s3Service.createPresignedUrl(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/s3/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.presignedUrl").value(fakeUrl));
    }

    @DisplayName("허용되지 않은 MIME 타입으로 요청하면 400 BAD REQUEST 를 반환한다")
    @Test
    void getS3PresignedUrl_실패_허용되지않은_MIME타입() throws Exception {
        // given
        PresignedUrlCreateRequest request = new PresignedUrlCreateRequest("application/pdf");

        given(s3Service.createPresignedUrl(any())).willThrow(new InvalidMimeTypeException());

        // when & then
        mockMvc.perform(post("/api/s3/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));
    }

    @DisplayName("요청 바디 없이 요청하면 400 을 반환한다")
    @Test
    void getS3PresignedUrl_실패_요청바디없음() throws Exception {
        mockMvc.perform(post("/api/s3/presigned-url")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
