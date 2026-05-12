package com.ddiring.ddiring_server.domain.family.presentation;

import com.ddiring.ddiring_server.domain.family.application.service.FamilyService;
import com.ddiring.ddiring_server.domain.family.presentation.dto.request.CreateFamilyRequest;
import com.ddiring.ddiring_server.domain.family.presentation.dto.request.JoinFamilyRequest;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.CreateFamilyResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.ElderListResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.MemberListResponse;
import com.ddiring.ddiring_server.domain.family.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "가족방", description = "가족방 구성원 관련 API")
@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @Operation(summary = "가족방 생성", description = "새 가족방을 만들고 6자리 초대코드를 발급합니다. 이미 가족방에 소속된 경우 생성 불가합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "가족방 생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateFamilyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가족방에 소속됨",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateFamilyResponse> createFamily(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CreateFamilyRequest request
    ) {
        CreateFamilyResponse response = familyService.createFamily(userId, request);
        return ApiResponse.success(HttpStatus.CREATED, ResponseMessage.FAMILY_CREATE_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "가족방 입장", description = "초대코드로 가족방에 입장합니다. 입장 후 보호자 승인이 필요합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "입장 요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유효하지 않은 초대코드",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가족방에 소속됨",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/join")
    public ApiResponse<Void> joinFamily(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid JoinFamilyRequest request
    ) {
        familyService.joinFamily(userId, request);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.FAMILY_JOIN_SUCCESS.getMessage());
    }

    @Operation(summary = "구성원 전체 목록 조회", description = "내 가족방의 모든 구성원을 조회합니다. 승인 대기(PENDING) 구성원도 포함됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MemberListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "소속된 가족방 없음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/members")
    public ApiResponse<MemberListResponse> getMembers(
            @AuthenticationPrincipal Long userId
    ) {
        MemberListResponse response = familyService.getMembers(userId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.MEMBER_LIST_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "연결된 어르신 목록 조회", description = "내 가족방에서 승인 완료된 어르신 목록을 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ElderListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "소속된 가족방 없음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/elders")
    public ApiResponse<ElderListResponse> getConnectedElders(
            @AuthenticationPrincipal Long userId
    ) {
        ElderListResponse response = familyService.getConnectedElders(userId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.ELDER_LIST_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "구성원 승인", description = "대기 중인 구성원을 승인합니다. 가족방 생성자(주 보호자)만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "승인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "방 생성자가 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "구성원을 찾을 수 없음",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("/members/{memberId}/approve")
    public ApiResponse<Void> approveMember(
            @Parameter(description = "승인할 구성원 ID") @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId
    ) {
        familyService.approveMember(userId, memberId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.MEMBER_APPROVE_SUCCESS.getMessage());
    }

    @Operation(summary = "구성원 거절/내보내기", description = "구성원을 가족방에서 제거합니다. 가족방 생성자(주 보호자)만 수행할 수 있습니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "거절/내보내기 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "방 생성자가 아님",
                    content = @Content(schema = @Schema())),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "구성원을 찾을 수 없음",
                    content = @Content(schema = @Schema()))
    })
    @DeleteMapping("/members/{memberId}")
    public ApiResponse<Void> rejectMember(
            @Parameter(description = "거절/내보낼 구성원 ID") @PathVariable Long memberId,
            @AuthenticationPrincipal Long userId
    ) {
        familyService.rejectMember(userId, memberId);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.MEMBER_REJECT_SUCCESS.getMessage());
    }
}
