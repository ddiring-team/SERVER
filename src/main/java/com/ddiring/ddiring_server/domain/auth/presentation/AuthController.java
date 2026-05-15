package com.ddiring.ddiring_server.domain.auth.presentation;

import com.ddiring.ddiring_server.domain.auth.application.service.AuthService;
import com.ddiring.ddiring_server.domain.auth.presentation.dto.request.*;
import com.ddiring.ddiring_server.domain.auth.presentation.dto.response.AuthResponse;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "보호자 회원가입", description = "아이디/비밀번호로 보호자 계정을 생성합니다. 가입 즉시 JWT 토큰을 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효성 검사 실패 (아이디 4~20자, 비밀번호 8~20자)",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 아이디 또는 전화번호",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/guardian/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> guardianSignup(@Valid @RequestBody GuardianSignupRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "회원가입이 완료되었습니다.", authService.guardianSignup(request));
    }

    @Operation(summary = "보호자 로그인", description = "아이디/비밀번호로 로그인합니다. JWT 토큰 만료 기간은 1일입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/guardian/login")
    public ApiResponse<AuthResponse> guardianLogin(@Valid @RequestBody GuardianLoginRequest request) {
        return ApiResponse.success(HttpStatus.OK, "로그인이 완료되었습니다.", authService.guardianLogin(request));
    }

    @Operation(summary = "어르신 최초 등록", description = "초대코드와 이름으로 어르신 계정을 생성합니다. 보호자 승인 전까지 PENDING 상태이며, JWT 토큰 만료 기간은 30일입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 초대코드",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "해당 가족방에 동일 이름 어르신 존재 또는 이미 사용 중인 전화번호",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/elder/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> elderRegister(@Valid @RequestBody ElderRegisterRequest request) {
        return ApiResponse.success(HttpStatus.CREATED, "등록이 완료되었습니다.", authService.elderRegister(request));
    }

    @Operation(summary = "어르신 로그인", description = "초대코드와 이름으로 재접속합니다. 비밀번호 없이 인증됩니다. JWT 토큰 만료 기간은 30일입니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 가족방에 일치하는 어르신 없음",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/elder/login")
    public ApiResponse<AuthResponse> elderLogin(@Valid @RequestBody ElderLoginRequest request) {
        return ApiResponse.success(HttpStatus.OK, "로그인이 완료되었습니다.", authService.elderLogin(request));
    }

    @Operation(summary = "카카오 보호자 프로필 완성",
            description = "카카오 최초 로그인 후 보호자 이름·전화번호·생년월일을 등록합니다. 새 JWT를 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 완성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 전화번호")
    })
    @PostMapping("/kakao/complete/guardian")
    public ApiResponse<AuthResponse> completeGuardianProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody KakaoGuardianCompleteRequest request) {
        return ApiResponse.success(HttpStatus.OK, "프로필 설정이 완료되었습니다.",
                authService.completeGuardianProfile(userId, request));
    }

    @Operation(summary = "카카오 어르신 프로필 완성",
            description = "카카오 최초 로그인 후 어르신 이름·전화번호·생년월일·초대코드를 등록합니다. 새 JWT를 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 완성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 초대코드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 전화번호")
    })
    @PostMapping("/kakao/complete/elder")
    public ApiResponse<AuthResponse> completeElderProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody KakaoElderCompleteRequest request) {
        return ApiResponse.success(HttpStatus.OK, "프로필 설정이 완료되었습니다.",
                authService.completeElderProfile(userId, request));
    }

    @Operation(summary = "로그아웃", description = "클라이언트 측 토큰을 삭제하도록 안내합니다. 서버는 별도의 토큰 무효화 없이 200을 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(HttpStatus.OK, "로그아웃되었습니다.", null);
    }
}
