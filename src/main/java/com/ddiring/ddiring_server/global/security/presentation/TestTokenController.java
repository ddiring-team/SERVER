package com.ddiring.ddiring_server.global.security.presentation;

import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import com.ddiring.ddiring_server.global.security.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트 전용", description = "테스트용 JWT 토큰 발급 API")
@RestController
@RequestMapping("/api/auth/test")
@RequiredArgsConstructor
public class TestTokenController {

    private final TokenProvider tokenProvider;

    @Operation(summary = "테스트용 JWT 토큰 발급", description = "userId로 JWT 토큰을 즉시 발급합니다. local/dev 환경 전용입니다.")
    @GetMapping("/token/{userId}")
    public ApiResponse<String> issueTestToken(@PathVariable Long userId) {
        String token = tokenProvider.createByUserId(userId);
        return ApiResponse.success(HttpStatus.OK, "테스트 토큰이 발급되었습니다.", token);
    }
}
