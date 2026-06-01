package com.ddiring.ddiring_server.domain.temperature.presentation;

import com.ddiring.ddiring_server.domain.temperature.application.service.TemperatureService;
import com.ddiring.ddiring_server.domain.temperature.presentation.dto.response.UserTemperatureResponse;
import com.ddiring.ddiring_server.domain.temperature.presentation.dto.response.WeeklyTemperatureResponse;
import com.ddiring.ddiring_server.domain.temperature.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "안부 온도", description = "어르신·보호자 안부 온도 API")
@RestController
@RequestMapping("/api/temperatures")
@RequiredArgsConstructor
public class TemperatureController {

    private final TemperatureService temperatureService;

    @Operation(summary = "내 안부 온도 조회", description = "출석·설문·사진 활동으로 누적된 내 안부 온도를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserTemperatureResponse.class)))
    })
    @GetMapping("/me")
    public ApiResponse<UserTemperatureResponse> getMyTemperature(
            @AuthenticationPrincipal Long userId
    ) {
        UserTemperatureResponse response = temperatureService.getMyTemperature(userId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.TEMPERATURE_GET_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "내 주간 안부 온도 변화 조회", description = "지난 주(월~일) 7일간의 일별 안부 온도를 조회합니다. 기록이 없는 날은 온도가 null입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = WeeklyTemperatureResponse.class)))
    })
    @GetMapping("/me/weekly")
    public ApiResponse<WeeklyTemperatureResponse> getMyWeeklyTemperature(
            @AuthenticationPrincipal Long userId
    ) {
        WeeklyTemperatureResponse response = temperatureService.getMyWeeklyTemperature(userId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.WEEKLY_TEMPERATURE_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "어르신 주간 안부 온도 변화 조회 (보호자)", description = "보호자가 같은 가족방 어르신의 지난 주(월~일) 일별 안부 온도를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = WeeklyTemperatureResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "같은 가족방의 어르신이 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방 멤버 정보 없음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/elders/{elderId}/weekly")
    public ApiResponse<WeeklyTemperatureResponse> getElderWeeklyTemperature(
            @Parameter(description = "어르신 사용자 ID") @PathVariable Long elderId,
            @AuthenticationPrincipal Long guardianId
    ) {
        WeeklyTemperatureResponse response = temperatureService.getElderWeeklyTemperature(guardianId, elderId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.WEEKLY_TEMPERATURE_SUCCESS.getMessage(), response);
    }
}
