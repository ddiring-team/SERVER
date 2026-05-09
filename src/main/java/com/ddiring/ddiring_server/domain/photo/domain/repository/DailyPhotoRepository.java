package com.ddiring.ddiring_server.domain.photo.domain.repository;

import com.ddiring.ddiring_server.domain.photo.domain.entity.DailyPhoto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyPhotoRepository extends JpaRepository<DailyPhoto, Long> {

    boolean existsByUser_IdAndFamily_IdAndTakenDate(Long userId, Long familyId, LocalDate takenDate);

    @EntityGraph(attributePaths = {"user"})
    List<DailyPhoto> findAllByFamily_IdAndTakenDateOrderByCreatedAtDesc(Long familyId, LocalDate takenDate);
}