package com.ddiring.ddiring_server.domain.survey.domain.entity;

import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.QuestionType;
import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "survey_question")
public class SurveyQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;              // 소속 설문

    @Column(length = 50)
    private String category;            // 질문 카테고리 (식사/수분, 활동/외출 등)

    @Column(length = 200)
    private String content;             // 질문 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;  // 질문 유형

    @Column(nullable = false)
    private Integer orderNum;           // 질문 순서
}
