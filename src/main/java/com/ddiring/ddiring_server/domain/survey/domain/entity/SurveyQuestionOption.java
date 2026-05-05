package com.ddiring.ddiring_server.domain.survey.domain.entity;

import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "survey_question_option")
public class SurveyQuestionOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;    // 소속 질문

    @Column(length = 50)
    private String label;               // 선택지 텍스트 (예: 괜찮아요)

    @Column(nullable = false)
    private Integer orderNum;           // 선택지 순서

}
