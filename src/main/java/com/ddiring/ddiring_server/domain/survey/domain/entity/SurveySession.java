package com.ddiring.ddiring_server.domain.survey.domain.entity;

import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.SurveyStatus;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "survey_session",
        uniqueConstraints = @UniqueConstraint(columnNames = {"elder_id", "survey_id", "session_date"}))
public class SurveySession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;              // 진행 중인 설문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private User elder;                 // 설문 수행 어르신

    @Column(nullable = false)
    private LocalDate sessionDate;      // 설문 수행 날짜 (하루 1회 제한 기준)

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SurveyStatus status = SurveyStatus.IN_PROGRESS; // 진행 중 / 완료

    private LocalDateTime completedAt;  // 설문 완료 시각
}
