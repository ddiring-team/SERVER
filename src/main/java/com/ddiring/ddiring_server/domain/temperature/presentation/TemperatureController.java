package com.ddiring.ddiring_server.domain.temperature.presentation;

import com.ddiring.ddiring_server.domain.temperature.application.service.TemperatureService;
import com.ddiring.ddiring_server.domain.temperature.presentation.dto.response.UserTemperatureResponse;
import com.ddiring.ddiring_server.domain.temperature.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "안부 온도", description = "어르신·보호자 안부 온도 API")
@RestController
@RequestMapping("/api/temperatures")
@RequiredArgsConstructor
public class TemperatureController {

    private final TemperatureService temperatureService;

    @Operation(summary = "내 안부 온도 조회", description = "출석·설문·사진 활동으로 누적된 내 안부 온도를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserTemperatureResponse> getMyTemperature(
            @AuthenticationPrincipal Long userId
    ) {
        UserTemperatureResponse response = temperatureService.getMyTemperature(userId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.TEMPERATURE_GET_SUCCESS.getMessage(), response);
    }
}
