package com.ddiring.ddiring_server.domain.alert.domain.entity;

import com.ddiring.ddiring_server.domain.alert.domain.entity.enums.AlertType;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "risk_alert_history",
        indexes = {
                @Index(name = "idx_alert_elder_type_created", columnList = "elder_id, alertType, createdAt")
        })
public class RiskAlertHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private User elder;                 // 위험 대상 어르신

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AlertType alertType;        // ATTENDANCE_INACTIVE | WEEKLY_RISK

    @Column(length = 50)
    private String category;            // 카테고리 (WEEKLY_RISK 전용, 출석은 null)

    @Column(nullable = false, length = 500)
    private String message;             // 발송된 알림 본문 (감사/디버깅용)
}
