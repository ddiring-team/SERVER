package com.ddiring.ddiring_server.domain.family.domain.repository;

import com.ddiring.ddiring_server.domain.family.domain.entity.FamilyMember;
import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    @Query("SELECT fm.family.id FROM FamilyMember fm WHERE fm.user.id = :userId")
    Optional<Long> findFamilyIdByUserId(@Param("userId") Long userId);

    boolean existsByUser_Id(Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<FamilyMember> findAllByFamily_Id(Long familyId);

    @EntityGraph(attributePaths = {"user"})
    List<FamilyMember> findAllByFamily_IdAndRoleAndStatus(Long familyId, Role role, MemberStatus status);

    @Query("SELECT fm FROM FamilyMember fm JOIN FETCH fm.user u WHERE fm.family.inviteCode = :inviteCode AND u.name = :name AND fm.role = 'ELDER'")
    Optional<FamilyMember> findElderByInviteCodeAndName(@Param("inviteCode") String inviteCode, @Param("name") String name);
}
