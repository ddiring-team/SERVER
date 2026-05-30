package com.ddiring.ddiring_server.domain.distance.domain.repository;

import com.ddiring.ddiring_server.domain.distance.domain.entity.PairDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PairDistanceRepository extends JpaRepository<PairDistance, Long> {

    @Query("""
        SELECT pd FROM PairDistance pd
        JOIN FETCH pd.elder
        JOIN FETCH pd.guardian
        WHERE pd.elder.id = :userId OR pd.guardian.id = :userId
        """)
    List<PairDistance> findAllByUserId(@Param("userId") Long userId);

    Optional<PairDistance> findByElder_IdAndGuardian_Id(Long elderId, Long guardianId);

    boolean existsByElder_IdAndGuardian_Id(Long elderId, Long guardianId);

    List<PairDistance> findAllByFamily_Id(Long familyId);

    void deleteAllByElder_IdOrGuardian_Id(Long elderId, Long guardianId);
}
