package com.ddiring.ddiring_server.domain.survey.domain.repository;

import com.ddiring.ddiring_server.domain.survey.domain.entity.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, Long> {

    @Query("SELECT sq.survey.id, COUNT(sq) FROM SurveyQuestion sq WHERE sq.survey.id IN :surveyIds GROUP BY sq.survey.id")
    List<Object[]> countGroupedBySurveyIds(@Param("surveyIds") Collection<Long> surveyIds);

    @Query("SELECT sq.id FROM SurveyQuestion sq WHERE sq.survey.id = :surveyId")
    List<Long> findIdsBySurveyId(@Param("surveyId") Long surveyId);

    List<SurveyQuestion> findAllBySurvey_IdOrderByOrderNumAsc(Long surveyId);

    @Modifying
    @Query("DELETE FROM SurveyQuestion sq WHERE sq.survey.id = :surveyId")
    void deleteAllBySurveyId(@Param("surveyId") Long surveyId);
}
