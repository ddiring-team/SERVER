package com.ddiring.ddiring_server.domain.survey.domain.entity;

import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transformed_question_cache",
        uniqueConstraints = @UniqueConstraint(columnNames = {"elder_id", "date", "question_key"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TransformedQuestionCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transformed_question_cache_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private User elder;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "question_key", nullable = false, length = 50)
    private String questionKey;

    @Column(name = "transformed", nullable = false, columnDefinition = "TEXT")
    private String transformed;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TransformedQuestionCache(User elder, LocalDate date, String questionKey, String transformed) {
        this.elder = elder;
        this.date = date;
        this.questionKey = questionKey;
        this.transformed = transformed;
    }
}
