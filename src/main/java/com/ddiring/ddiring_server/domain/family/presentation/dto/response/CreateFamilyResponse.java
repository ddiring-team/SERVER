package com.ddiring.ddiring_server.domain.family.presentation.dto.response;

import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가족방 생성 응답")
public record CreateFamilyResponse(
        @Schema(description = "가족방 ID", example = "1")
        Long familyId,
        @Schema(description = "가족방 이름", example = "우리 가족")
        String name,
        @Schema(description = "6자리 초대코드", example = "A3B7K2")
        String inviteCode
) {
    public static CreateFamilyResponse from(Family family) {
        return new CreateFamilyResponse(family.getId(), family.getName(), family.getInviteCode());
    }
}
