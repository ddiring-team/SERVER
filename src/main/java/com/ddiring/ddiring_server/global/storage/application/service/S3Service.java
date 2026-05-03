package com.ddiring.ddiring_server.global.storage.application.service;

import com.ddiring.ddiring_server.global.config.S3Config;
import com.ddiring.ddiring_server.global.storage.application.service.enums.AllowedMimeType;
import com.ddiring.ddiring_server.global.storage.exception.InvalidMimeTypeException;

import com.ddiring.ddiring_server.global.storage.presentation.dto.request.PresignedUrlCreateRequest;
import com.ddiring.ddiring_server.global.storage.presentation.dto.response.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Config s3Config;

    public PresignedUrlResponse createPresignedUrl(PresignedUrlCreateRequest presignedUrlCreateRequest) {
        String mimeType = presignedUrlCreateRequest.mimeType();
        String uuid = UUID.randomUUID().toString();
        String extension = getExtensionFromMimeType(mimeType);

        String key;
        if (mimeType.startsWith("image")){
            key = "image/" + uuid + "." + extension;
        } else{
            throw new InvalidMimeTypeException();
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(key)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder -> builder
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
        );

        return new PresignedUrlResponse(presignedRequest.url().toString());

    }

    private String getExtensionFromMimeType(String mimeType) {
        if (!AllowedMimeType.ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new InvalidMimeTypeException();
        }
        int slashIndex = mimeType.lastIndexOf('/');
        return mimeType.substring(slashIndex + 1);
    }


}
