package com.ddiring.ddiring_server.domain.survey.domain.repository;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveySession;
import com.ddiring.ddiring_server.domain.survey.domain.entity.enums.SurveyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface SurveySessionRepository extends JpaRepository<SurveySession, Long> {

    @Query("SELECT ss.survey.id, COUNT(ss) FROM SurveySession ss WHERE ss.survey.id IN :surveyIds AND ss.status = :status GROUP BY ss.survey.id")
    List<Object[]> countGroupedBySurveyIdsAndStatus(
            @Param("surveyIds") Collection<Long> surveyIds,
            @Param("status") SurveyStatus status
    );

    @Query("SELECT ss.id FROM SurveySession ss WHERE ss.survey.id = :surveyId")
    List<Long> findIdsBySurveyId(@Param("surveyId") Long surveyId);

    @Modifying
    @Query("DELETE FROM SurveySession ss WHERE ss.survey.id = :surveyId")
    void deleteAllBySurveyId(@Param("surveyId") Long surveyId);

    java.util.Optional<SurveySession> findByElder_IdAndSurvey_IdAndSessionDate(Long elderId, Long surveyId, LocalDate sessionDate);

    // 어르신의 최근 완료된 세션 조회 (날짜 내림차순). since는 포함 (inclusive).
    @Query("SELECT ss FROM SurveySession ss WHERE ss.elder.id = :elderId AND ss.status = 'COMPLETED' AND ss.sessionDate >= :since ORDER BY ss.sessionDate DESC")
    List<SurveySession> findRecentCompletedByElderId(@Param("elderId") Long elderId, @Param("since") LocalDate since);
}
