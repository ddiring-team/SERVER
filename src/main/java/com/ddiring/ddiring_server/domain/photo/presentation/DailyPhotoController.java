package com.ddiring.ddiring_server.domain.photo.presentation;

import com.ddiring.ddiring_server.domain.photo.application.service.DailyPhotoService;
import com.ddiring.ddiring_server.domain.photo.domain.entity.enums.EmojiType;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.request.CreateDailyPhotoRequest;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.response.DailyPhotoFeedResponse;
import com.ddiring.ddiring_server.domain.photo.presentation.dto.response.DailyPhotoResponse;
import com.ddiring.ddiring_server.domain.photo.presentation.message.ResponseMessage;
import com.ddiring.ddiring_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "일상 공유", description = "일상 공유 관련 API")
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class DailyPhotoController {

    private final DailyPhotoService dailyPhotoService;

    @Operation(summary = "일상 남기기", description = "오늘의 일상 사진을 가족방에 공유합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "일상 공유 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방 정보 없음",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping
    public ApiResponse<Void> createDailyPhoto(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateDailyPhotoRequest request
    ) {
        dailyPhotoService.createDailyPhoto(userId, request);
        return ApiResponse.success(HttpStatus.CREATED, ResponseMessage.DAILY_PHOTO_CREATE_SUCCESS.getMessage());
    }

    @Operation(summary = "일상 목록 조회", description = "가족방 구성원 전체의 일상 게시글을 날짜별로 조회합니다. date 미입력 시 오늘 날짜로 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = DailyPhotoResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방 정보 없음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping
    public ApiResponse<List<DailyPhotoResponse>> getDailyPhotos(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회 날짜 (예: 2026-05-09), 미입력 시 오늘")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        List<DailyPhotoResponse> response = dailyPhotoService.getDailyPhotos(userId, targetDate);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.DAILY_PHOTO_LIST_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "가족방 피드 조회 (무한 스크롤)", description = "가족방의 모든 게시글을 최신순으로 커서 기반 무한 스크롤 조회합니다. cursor 미입력 시 첫 페이지를 조회합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = DailyPhotoFeedResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가족방 정보 없음",
                    content = @Content(schema = @Schema()))
    })
    @GetMapping("/feed")
    public ApiResponse<DailyPhotoFeedResponse> getDailyPhotoFeed(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "이전 응답의 nextCursor 값 (첫 페이지는 생략)")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기 (기본값: 20)")
            @RequestParam(defaultValue = "20") int size
    ) {
        DailyPhotoFeedResponse response = dailyPhotoService.getDailyPhotoFeed(userId, cursor, size);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.DAILY_PHOTO_FEED_SUCCESS.getMessage(), response);
    }

    @Operation(summary = "이모지 반응 토글", description = "게시글에 이모지 반응을 추가하거나 취소합니다. 같은 이모지를 다시 누르면 취소됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "반응 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 가족방 정보 없음",
                    content = @Content(schema = @Schema()))
    })
    @PostMapping("/{photoId}/reactions/{emojiType}")
    public ApiResponse<Void> toggleReaction(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "게시글 ID") @PathVariable Long photoId,
            @Parameter(description = "이모지 타입 (HEART, SMILE, LAUGH, CRY)") @PathVariable EmojiType emojiType
    ) {
        dailyPhotoService.toggleReaction(userId, photoId, emojiType);
        return ApiResponse.success(HttpStatus.OK, ResponseMessage.REACTION_TOGGLE_SUCCESS.getMessage());
    }
}
