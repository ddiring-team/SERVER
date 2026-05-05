package com.ddiring.ddiring_server.domain.user.domain.entity;

import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "elder_profile")
public class ElderProfile extends BaseEntity {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;                  // users.id FK (1:1)

    private LocalTime checkinTime;      // 매일 체크인 알림 발송 시각

    @Builder.Default
    @Column(precision = 4, scale = 1)
    private BigDecimal temperature      // 활동 온도 (출석/설문/사진 업로드 시 상승)
            = BigDecimal.valueOf(36.5);

}
