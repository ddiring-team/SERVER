package com.ddiring.ddiring_server.domain.distance.application.service;

import com.ddiring.ddiring_server.domain.distance.application.event.DistanceResetEvent;
import com.ddiring.ddiring_server.domain.distance.domain.entity.PairDistance;
import com.ddiring.ddiring_server.domain.distance.domain.repository.PairDistanceRepository;
import com.ddiring.ddiring_server.domain.distance.presentation.dto.response.PairDistanceResponse;
import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import com.ddiring.ddiring_server.domain.family.domain.entity.FamilyMember;
import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistanceService {

    private final PairDistanceRepository pairDistanceRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional(readOnly = true)
    public List<PairDistanceResponse> getMyDistances(Long userId) {
        return pairDistanceRepository.findAllByUserId(userId).stream()
                .map(pd -> PairDistanceResponse.of(pd, userId))
                .toList();
    }

    /**
     * 가족방 멤버 승인 시 호출. 승인된 멤버와 반대 역할 APPROVED 멤버들 사이에 페어를 생성한다.
     */
    @Transactional
    public void createPairsForApprovedMember(FamilyMember approvedMember) {
        Long familyId = approvedMember.getFamily().getId();
        Role oppositeRole = approvedMember.getRole() == Role.ELDER ? Role.GUARDIAN : Role.ELDER;

        List<FamilyMember> counterparts = familyMemberRepository
                .findAllByFamily_IdAndRoleAndStatus(familyId, oppositeRole, MemberStatus.APPROVED);

        for (FamilyMember counterpart : counterparts) {
            User elder = approvedMember.getRole() == Role.ELDER ? approvedMember.getUser() : counterpart.getUser();
            User guardian = approvedMember.getRole() == Role.GUARDIAN ? approvedMember.getUser() : counterpart.getUser();

            if (pairDistanceRepository.existsByElder_IdAndGuardian_Id(elder.getId(), guardian.getId())) {
                continue;
            }
            pairDistanceRepository.save(PairDistance.builder()
                    .elder(elder)
                    .guardian(guardian)
                    .family(approvedMember.getFamily())
                    .build());
        }
    }

    @Transactional
    public void deletePairsForUser(Long userId) {
        pairDistanceRepository.deleteAllByElder_IdOrGuardian_Id(userId, userId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReset(DistanceResetEvent event) {
        List<PairDistance> targets = (event.targetUserId() == null)
                ? pairDistanceRepository.findAllByUserId(event.actorUserId())
                : findSpecificPair(event.actorUserId(), event.targetUserId());

        for (PairDistance pd : targets) {
            pd.reset(event.actionType());
        }
    }

    private List<PairDistance> findSpecificPair(Long a, Long b) {
        return pairDistanceRepository.findByElder_IdAndGuardian_Id(a, b)
                .or(() -> pairDistanceRepository.findByElder_IdAndGuardian_Id(b, a))
                .map(List::of)
                .orElse(List.of());
    }
}
