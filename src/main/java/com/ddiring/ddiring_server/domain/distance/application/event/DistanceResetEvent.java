package com.ddiring.ddiring_server.domain.distance.application.event;

import com.ddiring.ddiring_server.domain.distance.domain.entity.enums.DistanceActionType;

/**
 * 거리 리셋 이벤트.
 * - targetUserId가 null이면 actorUserId가 포함된 모든 페어를 리셋한다.
 * - targetUserId가 있으면 (actorUserId, targetUserId) 특정 페어만 리셋한다 (예: 보호자가 특정 어르신 사진 조회).
 */
public record DistanceResetEvent(
        Long actorUserId,
        Long targetUserId,
        DistanceActionType actionType
) {
    public static DistanceResetEvent broadcast(Long actorUserId, DistanceActionType actionType) {
        return new DistanceResetEvent(actorUserId, null, actionType);
    }

    public static DistanceResetEvent pair(Long actorUserId, Long targetUserId, DistanceActionType actionType) {
        return new DistanceResetEvent(actorUserId, targetUserId, actionType);
    }
}
