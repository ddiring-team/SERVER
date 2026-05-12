package com.ddiring.ddiring_server.domain.family.presentation.dto.response;

import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가족방 가입 상태 응답")
public record FamilyStatusResponse(
        @Schema(description = "가족방 가입 여부", example = "true")
        boolean inFamily,
        @Schema(description = "승인 상태 (PENDING: 대기, APPROVED: 승인, 미가입 시 null)", example = "APPROVED", nullable = true)
        MemberStatus status
) {

    public static FamilyStatusResponse notInFamily() {
        return new FamilyStatusResponse(false, null);
    }

    public static FamilyStatusResponse inFamily(MemberStatus status) {
        return new FamilyStatusResponse(true, status);
    }
}
