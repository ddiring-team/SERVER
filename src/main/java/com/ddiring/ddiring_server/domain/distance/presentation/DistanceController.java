package com.ddiring.ddiring_server.domain.distance.presentation;

import com.ddiring.ddiring_server.domain.distance.application.service.DistanceService;
import com.ddiring.ddiring_server.domain.distance.presentation.dto.response.PairDistanceResponse;
import com.ddiring.ddiring_server.domain.distance.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "안부거리", description = "어르신-보호자 간 안부거리 API")
@RestController
@RequestMapping("/api/distances")
@RequiredArgsConstructor
public class DistanceController {

    private final DistanceService distanceService;

    @Operation(summary = "내 안부거리 목록 조회", description = "내가 속한 모든 어르신-보호자 페어의 현재 거리를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PairDistanceResponse.class)))
    })
    @GetMapping("/me")
    public ApiResponse<List<PairDistanceResponse>> getMyDistances(
            @AuthenticationPrincipal Long userId
    ) {
        List<PairDistanceResponse> response = distanceService.getMyDistances(userId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.DISTANCE_LIST_SUCCESS.getMessage(), response);
    }
}
