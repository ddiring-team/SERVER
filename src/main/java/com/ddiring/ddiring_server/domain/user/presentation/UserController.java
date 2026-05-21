package com.ddiring.ddiring_server.domain.user.presentation;

import com.ddiring.ddiring_server.domain.user.application.service.UserService;
import com.ddiring.ddiring_server.domain.user.presentation.dto.request.UpdateFcmTokenRequest;
import com.ddiring.ddiring_server.domain.user.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "FCM 토큰 등록/갱신",
            description = "기기의 FCM 토큰을 서버에 등록합니다. 앱 시작 또는 토큰 갱신 시 호출하세요. 기존 토큰이 있으면 덮어씁니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "FCM 토큰 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "FCM 토큰 누락",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema()))
    })
    @PutMapping("/fcm-token")
    public ApiResponse<Void> updateFcmToken(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateFcmTokenRequest request
    ) {
        userService.updateFcmToken(userId, request.fcmToken());
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.FCM_TOKEN_UPDATED.getMessage());
    }
}
