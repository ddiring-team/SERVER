package com.ddiring.ddiring_server.domain.family.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "연결된 어르신 목록 응답")
public record ElderListResponse(
        @Schema(description = "승인 완료된 어르신 목록")
        List<FamilyMemberResponse> elders
) {
    public static ElderListResponse of(List<FamilyMemberResponse> elders) {
        return new ElderListResponse(elders);
    }
}
