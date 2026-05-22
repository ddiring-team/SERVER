package com.ddiring.ddiring_server.domain.survey.domain.repository;

import com.ddiring.ddiring_server.domain.survey.domain.entity.TransformedQuestionCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransformedQuestionCacheRepository extends JpaRepository<TransformedQuestionCache, Long> {

    @Query("SELECT c FROM TransformedQuestionCache c " +
            "WHERE c.elder.id = :elderId AND c.date = :date AND c.questionKey IN :questionKeys")
    List<TransformedQuestionCache> findByElderIdAndDateAndQuestionKeyIn(
            @Param("elderId") Long elderId,
            @Param("date") LocalDate date,
            @Param("questionKeys") List<String> questionKeys
    );
}
