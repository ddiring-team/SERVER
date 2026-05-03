package com.ddiring.ddiring_server.global.storage.application.service;

import com.ddiring.ddiring_server.global.config.S3Config;
import com.ddiring.ddiring_server.global.storage.exception.InvalidMimeTypeException;
import com.ddiring.ddiring_server.global.storage.presentation.dto.request.PresignedUrlCreateRequest;
import com.ddiring.ddiring_server.global.storage.presentation.dto.response.PresignedUrlResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Config s3Config;

    @InjectMocks
    private S3Service s3Service;

    @DisplayName("유효한 MIME 타입으로 Presigned URL 발급에 성공한다")
    @ParameterizedTest(name = "MIME 타입: {0}")
    @ValueSource(strings = {"image/jpeg", "image/png", "image/jpg", "image/webp"})
    void createPresignedUrl_성공(String mimeType) throws MalformedURLException {
        // given
        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        URL mockUrl = new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/image/uuid.jpeg");

        given(s3Config.getBucket()).willReturn("test-bucket");
        given(s3Presigner.presignPutObject(any(Consumer.class))).willReturn(mockPresignedRequest);
        given(mockPresignedRequest.url()).willReturn(mockUrl);

        // when
        PresignedUrlResponse response = s3Service.createPresignedUrl(new PresignedUrlCreateRequest(mimeType));

        // then
        assertThat(response.presignedUrl()).isNotNull();
        assertThat(response.presignedUrl()).startsWith("https://");
    }

    @DisplayName("허용되지 않은 MIME 타입으로 요청하면 InvalidMimeTypeException 이 발생한다")
    @ParameterizedTest(name = "MIME 타입: {0}")
    @ValueSource(strings = {"application/pdf", "video/mp4", "text/plain", "image/gif"})
    void createPresignedUrl_실패_허용되지않은_MIME타입(String mimeType) {
        // when & then
        assertThatThrownBy(() -> s3Service.createPresignedUrl(new PresignedUrlCreateRequest(mimeType)))
                .isInstanceOf(InvalidMimeTypeException.class);
    }

    @DisplayName("이미지 파일의 S3 key는 image/ 경로로 시작한다")
    @Test
    void createPresignedUrl_이미지_키_경로_검증() throws MalformedURLException {
        // given
        String mimeType = "image/jpeg";
        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/image/some-uuid.jpeg");

        given(s3Config.getBucket()).willReturn("test-bucket");
        given(s3Presigner.presignPutObject(any(Consumer.class))).willReturn(mockPresignedRequest);
        given(mockPresignedRequest.url()).willReturn(mockUrl);

        // when
        PresignedUrlResponse response = s3Service.createPresignedUrl(new PresignedUrlCreateRequest(mimeType));

        // then
        assertThat(response.presignedUrl()).contains("/image/");
    }

    @DisplayName("Presigned URL 은 null 이 아니고 비어있지 않다")
    @Test
    void createPresignedUrl_응답값_비어있지않음() throws MalformedURLException {
        // given
        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/image/uuid.png?X-Amz-Algorithm=AWS4");

        given(s3Config.getBucket()).willReturn("test-bucket");
        given(s3Presigner.presignPutObject(any(Consumer.class))).willReturn(mockPresignedRequest);
        given(mockPresignedRequest.url()).willReturn(mockUrl);

        // when
        PresignedUrlResponse response = s3Service.createPresignedUrl(new PresignedUrlCreateRequest("image/png"));

        // then
        assertThat(response.presignedUrl()).isNotBlank();
    }
}