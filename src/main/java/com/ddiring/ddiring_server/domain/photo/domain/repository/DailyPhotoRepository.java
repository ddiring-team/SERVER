package com.ddiring.ddiring_server.domain.photo.domain.repository;

import com.ddiring.ddiring_server.domain.photo.domain.entity.DailyPhoto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyPhotoRepository extends JpaRepository<DailyPhoto, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<DailyPhoto> findAllByFamily_IdAndTakenDateOrderByCreatedAtDesc(Long familyId, LocalDate takenDate);

    @EntityGraph(attributePaths = {"user"})
    List<DailyPhoto> findByFamily_IdOrderByIdDesc(Long familyId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM DailyPhoto p WHERE p.family.id = :familyId AND p.id < :lastId ORDER BY p.id DESC")
    List<DailyPhoto> findByFamily_IdAndIdLessThanOrderByIdDesc(
            @Param("familyId") Long familyId,
            @Param("lastId") Long lastId,
            Pageable pageable);
}