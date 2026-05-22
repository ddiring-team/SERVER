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

    @Query("SELECT CASE WHEN COUNT(ss) > 0 THEN true ELSE false END FROM SurveySession ss WHERE ss.elder.id = :elderId AND ss.survey.id = :surveyId AND ss.sessionDate = :date AND ss.status = 'COMPLETED'")
    boolean existsCompletedByElderAndSurveyAndDate(@Param("elderId") Long elderId, @Param("surveyId") Long surveyId, @Param("date") LocalDate date);

    // 세션의 어르신이 특정 가족에 소속되어 있는지 확인
    @Query("SELECT CASE WHEN COUNT(ss) > 0 THEN true ELSE false END FROM SurveySession ss JOIN FamilyMember fm ON fm.user.id = ss.elder.id WHERE ss.id = :sessionId AND fm.family.id = :familyId")
    boolean existsByIdAndElderFamilyId(@Param("sessionId") Long sessionId, @Param("familyId") Long familyId);

    // 어르신의 완료된 세션 목록 조회 (날짜 내림차순, survey fetch join)
    @Query("SELECT ss FROM SurveySession ss JOIN FETCH ss.survey WHERE ss.elder.id = :elderId AND ss.status = 'COMPLETED' ORDER BY ss.sessionDate DESC, ss.completedAt DESC")
    List<SurveySession> findCompletedByElderIdOrderByDateDesc(@Param("elderId") Long elderId);

    // 어르신의 최근 완료된 세션 조회 (날짜 내림차순). since는 포함 (inclusive).
    @Query("SELECT ss FROM SurveySession ss WHERE ss.elder.id = :elderId AND ss.status = 'COMPLETED' AND ss.sessionDate >= :since ORDER BY ss.sessionDate DESC")
    List<SurveySession> findRecentCompletedByElderId(@Param("elderId") Long elderId, @Param("since") LocalDate since);

    // 어르신의 완료된 세션 조회 (특정 날짜 범위, 날짜 오름차순)
    @Query("SELECT ss FROM SurveySession ss WHERE ss.elder.id = :elderId AND ss.status = 'COMPLETED' AND ss.sessionDate BETWEEN :startDate AND :endDate ORDER BY ss.sessionDate ASC")
    List<SurveySession> findCompletedByElderIdAndDateRange(
            @Param("elderId") Long elderId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
