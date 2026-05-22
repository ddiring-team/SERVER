package com.ddiring.ddiring_server.domain.survey.domain.repository;

import com.ddiring.ddiring_server.domain.survey.domain.entity.Survey;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @EntityGraph(attributePaths = {"createdBy"})
    List<Survey> findAllByFamily_IdOrderByCreatedAtDesc(@Param("familyId") Long familyId);
}
