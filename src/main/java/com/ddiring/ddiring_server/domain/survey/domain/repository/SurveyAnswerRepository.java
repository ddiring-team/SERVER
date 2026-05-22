package com.ddiring.ddiring_server.domain.survey.domain.repository;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {

    // 세션의 답변 목록을 [카테고리, 답변텍스트] 형태로 조회 (카테고리가 있는 질문만, 질문 순서 오름차순)
    @Query("""
            SELECT sq.category, COALESCE(sqo.label, sa.answerText)
            FROM SurveyAnswer sa
            JOIN sa.question sq
            LEFT JOIN sa.selectedOption sqo
            WHERE sa.session.id = :sessionId AND sq.category IS NOT NULL
            ORDER BY sq.orderNum ASC
            """)
    List<Object[]> findCategoryAnswersBySessionId(@Param("sessionId") Long sessionId);

    @Modifying
    @Query("DELETE FROM SurveyAnswer sa WHERE sa.session.id IN :sessionIds")
    void deleteAllBySessionIdIn(@Param("sessionIds") Collection<Long> sessionIds);
}
