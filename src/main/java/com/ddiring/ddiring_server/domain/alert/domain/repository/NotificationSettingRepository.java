package com.ddiring.ddiring_server.domain.alert.domain.repository;

import com.ddiring.ddiring_server.domain.alert.domain.entity.NotificationSetting;
import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.NotificationType;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    List<NotificationSetting> findAllByUser_Id(Long userId);

    Optional<NotificationSetting> findByUser_IdAndType(Long userId, NotificationType type);

    /**
     * 특정 가족 내 지정 역할 구성원 중, 해당 알림 타입을 끄지 않은(opt-out 없음) 사용자의 FCM 토큰만 조회한다.
     * 설정 행이 없으면 기본 ON으로 간주해 포함한다.
     */
    @Query("SELECT u.fcmToken FROM FamilyMember fm JOIN fm.user u " +
            "WHERE fm.family.id = :familyId AND fm.role = :role " +
            "AND fm.status = com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus.APPROVED " +
            "AND u.fcmToken IS NOT NULL " +
            "AND NOT EXISTS (SELECT 1 FROM NotificationSetting ns " +
            "                WHERE ns.user.id = u.id AND ns.type = :type AND ns.enabled = false)")
    List<String> findEnabledFcmTokensByFamilyAndRoleAndType(@Param("familyId") Long familyId,
                                                            @Param("role") Role role,
                                                            @Param("type") NotificationType type);
}
