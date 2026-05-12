package com.ddiring.ddiring_server.domain.family.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "가족방 입장 요청")
public record JoinFamilyRequest(

        @Schema(description = "초대코드 (6자리)", example = "ABC123")
        @NotBlank
        @Size(min = 6, max = 6)
        String inviteCode
) {
}
