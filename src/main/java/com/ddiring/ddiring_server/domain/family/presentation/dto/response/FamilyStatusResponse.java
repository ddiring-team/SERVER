package com.ddiring.ddiring_server.domain.family.presentation.dto.response;

import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;

public record FamilyStatusResponse(boolean inFamily, MemberStatus status) {

    public static FamilyStatusResponse notInFamily() {
        return new FamilyStatusResponse(false, null);
    }

    public static FamilyStatusResponse inFamily(MemberStatus status) {
        return new FamilyStatusResponse(true, status);
    }
}
