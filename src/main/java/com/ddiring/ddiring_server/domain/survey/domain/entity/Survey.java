package com.ddiring.ddiring_server.domain.survey.domain.entity;

import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "survey")
public class Survey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;              // 소속 가족방

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;             // 설문 생성한 보호자

    @Column(nullable = false, length = 100)
    private String title;               // 설문 제목

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;    // 활성 여부

    public void activate() {
        this.isActive = true;
    }
}
