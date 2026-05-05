package com.ddiring.ddiring_server.domain.family.domain.entity;


import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "family_member",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class FamilyMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;              // 소속 가족방

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;                  // 유저 (한 유저는 한 방만 가능)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                  // 방 내 역할

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.PENDING; // 승인 대기 / 승인 완료

    private LocalDateTime joinedAt;     // 승인 완료 시각



}
