package com.ddiring.ddiring_server.domain.user.presentation.dto.response;

import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.Period;

@Schema(description = "내 정보 응답")
public record MyInfoResponse(
        @Schema(description = "이름") String name,
        @Schema(description = "전화번호") String phone,
        @Schema(description = "만나이") Integer age,
        @Schema(description = "역할 (GUARDIAN/ELDER)") Role role,
        @Schema(description = "프로필 이미지 URL") String profileImageUrl
) {
    public static MyInfoResponse from(User user) {
        LocalDate birthDate = user.getBirthDate();
        Integer age = (birthDate != null) ? Period.between(birthDate, LocalDate.now()).getYears() : null;

        return new MyInfoResponse(
                user.getName(),
                user.getPhone(),
                age,
                user.getRole(),
                user.getProfileImageUrl()
        );
    }
}
