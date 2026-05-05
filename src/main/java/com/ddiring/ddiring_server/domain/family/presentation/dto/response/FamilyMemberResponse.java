package com.ddiring.ddiring_server.domain.family.presentation.dto.response;

import com.ddiring.ddiring_server.domain.family.domain.entity.FamilyMember;
import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "가족방 구성원 정보")
public record FamilyMemberResponse(
        @Schema(description = "구성원 레코드 ID", example = "1")
        Long memberId,
        @Schema(description = "사용자 ID", example = "3")
        Long userId,
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,
        @Schema(description = "역할 (GUARDIAN: 보호자, ELDER: 어르신)", example = "GUARDIAN")
        Role role,
        @Schema(description = "가입 상태 (PENDING: 대기, APPROVED: 승인)", example = "APPROVED")
        MemberStatus status,
        @Schema(description = "승인 완료 시각 (대기 중이면 null)", example = "2026-05-05T10:00:00")
        LocalDateTime joinedAt
) {
    public static FamilyMemberResponse from(FamilyMember member) {
        return new FamilyMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getName(),
                member.getRole(),
                member.getStatus(),
                member.getJoinedAt()
        );
    }
}
