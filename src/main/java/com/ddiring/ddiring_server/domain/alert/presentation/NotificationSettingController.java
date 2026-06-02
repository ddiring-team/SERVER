package com.ddiring.ddiring_server.domain.alert.presentation;

import com.ddiring.ddiring_server.domain.alert.application.service.NotificationSettingService;
import com.ddiring.ddiring_server.domain.alert.presentation.dto.request.UpdateNotificationSettingRequest;
import com.ddiring.ddiring_server.domain.alert.presentation.dto.response.NotificationSettingResponse;
import com.ddiring.ddiring_server.domain.alert.presentation.message.ResponseMessage;
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

import java.util.List;

@Tag(name = "알림 설정", description = "알림 종류별 수신 on/off 설정 API")
@RestController
@RequestMapping("/api/users/me/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @Operation(
            summary = "내 알림 설정 조회",
            description = "로그인한 사용자의 알림 타입별 수신 여부를 반환합니다. 한 번도 변경한 적 없는 타입은 기본값 ON(true)으로 내려갑니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 설정 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping
    public ApiResponse<List<NotificationSettingResponse>> getMySettings(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.success(HttpStatus.OK,
                ResponseMessage.NOTIFICATION_SETTINGS_FETCHED.getMessage(),
                notificationSettingService.getMySettings(userId));
    }

    @Operation(
            summary = "알림 설정 변경",
            description = "특정 알림 타입의 수신 여부를 켜거나 끕니다. 설정이 없으면 새로 생성하고, 있으면 갱신합니다.",
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 설정 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (타입/수신 여부 누락)",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "동시 요청으로 인한 설정 충돌 (재시도 필요)",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping
    public ApiResponse<NotificationSettingResponse> updateSetting(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateNotificationSettingRequest request
    ) {
        return ApiResponse.success(HttpStatus.OK,
                ResponseMessage.NOTIFICATION_SETTING_UPDATED.getMessage(),
                notificationSettingService.updateSetting(userId, request.type(), request.enabled()));
    }
}
