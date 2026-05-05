package com.ddiring.ddiring_server.domain.family.domain.repository;

import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {

    boolean existsByInviteCode(String inviteCode);

    Optional<Family> findByInviteCode(String inviteCode);
}
