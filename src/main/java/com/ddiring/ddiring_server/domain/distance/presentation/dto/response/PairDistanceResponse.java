package com.ddiring.ddiring_server.domain.distance.presentation.dto.response;

import com.ddiring.ddiring_server.domain.distance.domain.entity.PairDistance;
import com.ddiring.ddiring_server.domain.distance.domain.entity.enums.DistanceActionType;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;

import java.time.LocalDateTime;

public record PairDistanceResponse(
        Long pairDistanceId,
        Long counterpartUserId,
        String counterpartName,
        String counterpartRole,
        String counterpartProfileImageUrl,
        int distanceKm,
        LocalDateTime lastActionAt,
        DistanceActionType lastActionType
) {
    public static PairDistanceResponse of(PairDistance pd, Long viewerId) {
        User counterpart = pd.getElder().getId().equals(viewerId) ? pd.getGuardian() : pd.getElder();
        return new PairDistanceResponse(
                pd.getId(),
                counterpart.getId(),
                counterpart.getName(),
                counterpart.getRole() != null ? counterpart.getRole().name() : null,
                counterpart.getProfileImageUrl(),
                pd.getDistanceKm(),
                pd.getLastActionAt(),
                pd.getLastActionType()
        );
    }
}
