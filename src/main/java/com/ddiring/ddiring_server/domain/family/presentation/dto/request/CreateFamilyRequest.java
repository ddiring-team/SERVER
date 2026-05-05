package com.ddiring.ddiring_server.domain.family.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "가족방 생성 요청")
public record CreateFamilyRequest(
        @Schema(description = "가족방 이름", example = "우리 가족")
        @NotBlank @Size(max = 100)
        String name
) {}
