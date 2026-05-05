package com.ddiring.ddiring_server.domain.survey.domain.entity;

import com.ddiring.ddiring_server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "survey_answer")
public class SurveyAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private SurveySession session;      // 소속 세션

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private SurveyQuestion question;    // 소속 질문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private SurveyQuestionOption selectedOption; // 선택형 응답 (TEXT 타입은 null)

    @Column(columnDefinition = "TEXT")
    private String answerText;          // 자유입력 응답 원문 (TEXT 타입만 사용)

    @Column(columnDefinition = "TEXT")
    private String aiComment;           // AI 생성 코멘트 (기억 기반 피드백)
}
