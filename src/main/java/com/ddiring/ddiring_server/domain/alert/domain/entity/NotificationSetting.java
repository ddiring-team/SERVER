package com.ddiring.ddiring_server.domain.alert.domain.entity;

import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.NotificationType;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자별 알림 수신 설정. 행이 존재하지 않으면 기본 ON(opt-out)으로 간주하고,
 * enabled=false 인 행만 발송 대상에서 제외한다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "notification_setting",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_setting_user_type", columnNames = {"user_id", "type"})
        })
public class NotificationSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false)
    private boolean enabled;

    public void updateEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
