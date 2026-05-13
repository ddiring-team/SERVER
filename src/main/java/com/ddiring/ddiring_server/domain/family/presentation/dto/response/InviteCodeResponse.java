package com.ddiring.ddiring_server.domain.family.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "초대코드 조회 응답")
public record InviteCodeResponse(
        @Schema(description = "6자리 초대코드", example = "A3B7K2")
        String inviteCode
) {
    public static InviteCodeResponse of(String inviteCode) {
        return new InviteCodeResponse(inviteCode);
    }
}