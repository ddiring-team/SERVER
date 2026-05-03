package com.ddiring.ddiring_server.global.storage.presentation;

import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import com.ddiring.ddiring_server.global.storage.application.service.S3Service;
import com.ddiring.ddiring_server.global.storage.presentation.dto.request.PresignedUrlCreateRequest;
import com.ddiring.ddiring_server.global.storage.presentation.dto.response.PresignedUrlResponse;
import com.ddiring.ddiring_server.global.storage.presentation.message.ResponseMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "S3", description = "S3 관련 API")
@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "S3 Presigned URL 발급", description = "파일 업로드를 위한 S3 Presigned URL을 발급합니다. 유효 시간은 10분입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공",
                    content = @Content(schema = @Schema(implementation = PresignedUrlResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "올바르지 않은 MIME 타입",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponse> getS3PresignedUrl(@RequestBody PresignedUrlCreateRequest presignedUrlCreateRequest){
        PresignedUrlResponse response = s3Service.createPresignedUrl(presignedUrlCreateRequest);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.PRESIGNED_URL_CREATE_SUCCESS.getMessage(), response);
    }
}
