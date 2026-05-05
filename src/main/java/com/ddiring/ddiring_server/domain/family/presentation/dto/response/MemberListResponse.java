package com.ddiring.ddiring_server.domain.family.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "구성원 전체 목록 응답")
public record MemberListResponse(
        @Schema(description = "구성원 목록 (PENDING 포함)")
        List<FamilyMemberResponse> members
) {
    public static MemberListResponse of(List<FamilyMemberResponse> members) {
        return new MemberListResponse(members);
    }
}
