package com.ddiring.ddiring_server.domain.distance.domain.entity;

import com.ddiring.ddiring_server.domain.distance.domain.entity.enums.DistanceActionType;
import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "pair_distance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_pair_distance_elder_guardian",
                columnNames = {"elder_user_id", "guardian_user_id"}
        ),
        indexes = {
                @Index(name = "idx_pair_distance_elder", columnList = "elder_user_id"),
                @Index(name = "idx_pair_distance_guardian", columnList = "guardian_user_id")
        })
public class PairDistance extends BaseEntity {

    public static final int MAX_DISTANCE_KM = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pair_distance_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_user_id", nullable = false)
    private User elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_user_id", nullable = false)
    private User guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Builder.Default
    @Column(nullable = false)
    private int distanceKm = 0;

    private LocalDateTime lastActionAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DistanceActionType lastActionType;

    private LocalDateTime maxReachedNotifiedAt;

    public void reset(DistanceActionType actionType) {
        this.distanceKm = 0;
        this.lastActionAt = LocalDateTime.now();
        this.lastActionType = actionType;
        this.maxReachedNotifiedAt = null;
    }

    /** @return true if the distance was actually incremented (i.e., not already saturated) */
    public boolean increment() {
        if (this.distanceKm >= MAX_DISTANCE_KM) {
            return false;
        }
        this.distanceKm++;
        return true;
    }

    public void markMaxNotified() {
        this.maxReachedNotifiedAt = LocalDateTime.now();
    }

    public boolean shouldNotifyMax() {
        return this.distanceKm >= MAX_DISTANCE_KM && this.maxReachedNotifiedAt == null;
    }
}
