package com.ddiring.ddiring_server.domain.distance.application.service;

import com.ddiring.ddiring_server.domain.distance.application.event.DistanceResetEvent;
import com.ddiring.ddiring_server.domain.distance.domain.entity.PairDistance;
import com.ddiring.ddiring_server.domain.distance.domain.entity.enums.DistanceActionType;
import com.ddiring.ddiring_server.domain.distance.domain.repository.PairDistanceRepository;
import com.ddiring.ddiring_server.domain.distance.presentation.dto.response.PairDistanceResponse;
import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import com.ddiring.ddiring_server.domain.family.domain.entity.FamilyMember;
import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DistanceServiceTest {

    @Mock private PairDistanceRepository pairDistanceRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;

    @InjectMocks
    private DistanceService distanceService;

    private User user(Long id, String name, Role role) {
        User u = User.builder().name(name).role(role).build();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private Family family(Long id) {
        Family f = Family.builder().inviteCode("CODE12").build();
        ReflectionTestUtils.setField(f, "id", id);
        return f;
    }

    @DisplayName("getMyDistances 는 내가 속한 페어를 상대방 기준으로 매핑하여 반환한다")
    @Test
    void getMyDistances_정상() {
        Long viewerId = 1L;
        User elder = user(viewerId, "어르신", Role.ELDER);
        User guardian = user(2L, "보호자", Role.GUARDIAN);
        PairDistance pd = PairDistance.builder()
                .elder(elder).guardian(guardian).family(family(10L)).distanceKm(3).build();
        ReflectionTestUtils.setField(pd, "id", 100L);

        given(pairDistanceRepository.findAllByUserId(viewerId)).willReturn(List.of(pd));

        List<PairDistanceResponse> result = distanceService.getMyDistances(viewerId);

        assertThat(result).hasSize(1);
        PairDistanceResponse r = result.get(0);
        assertThat(r.counterpartUserId()).isEqualTo(2L);
        assertThat(r.counterpartName()).isEqualTo("보호자");
        assertThat(r.counterpartRole()).isEqualTo("GUARDIAN");
        assertThat(r.distanceKm()).isEqualTo(3);
    }

    @DisplayName("ELDER 멤버가 승인되면 가족방의 모든 APPROVED GUARDIAN 과 페어가 생성된다")
    @Test
    void createPairsForApprovedMember_ELDER() {
        Family fam = family(10L);
        User elder = user(1L, "어르신", Role.ELDER);
        User g1 = user(2L, "보호자1", Role.GUARDIAN);
        User g2 = user(3L, "보호자2", Role.GUARDIAN);

        FamilyMember approvedElder = FamilyMember.builder().family(fam).user(elder).role(Role.ELDER).build();
        FamilyMember guard1 = FamilyMember.builder().family(fam).user(g1).role(Role.GUARDIAN).build();
        FamilyMember guard2 = FamilyMember.builder().family(fam).user(g2).role(Role.GUARDIAN).build();

        given(familyMemberRepository.findAllByFamily_IdAndRoleAndStatus(10L, Role.GUARDIAN, MemberStatus.APPROVED))
                .willReturn(List.of(guard1, guard2));
        given(pairDistanceRepository.existsByElder_IdAndGuardian_Id(any(), any())).willReturn(false);

        distanceService.createPairsForApprovedMember(approvedElder);

        verify(pairDistanceRepository, times(2)).save(any(PairDistance.class));
    }

    @DisplayName("이미 존재하는 페어는 중복 생성되지 않는다")
    @Test
    void createPairsForApprovedMember_중복방지() {
        Family fam = family(10L);
        User elder = user(1L, "어르신", Role.ELDER);
        User g1 = user(2L, "보호자1", Role.GUARDIAN);

        FamilyMember approvedElder = FamilyMember.builder().family(fam).user(elder).role(Role.ELDER).build();
        FamilyMember guard1 = FamilyMember.builder().family(fam).user(g1).role(Role.GUARDIAN).build();

        given(familyMemberRepository.findAllByFamily_IdAndRoleAndStatus(10L, Role.GUARDIAN, MemberStatus.APPROVED))
                .willReturn(List.of(guard1));
        given(pairDistanceRepository.existsByElder_IdAndGuardian_Id(1L, 2L)).willReturn(true);

        distanceService.createPairsForApprovedMember(approvedElder);

        verify(pairDistanceRepository, never()).save(any(PairDistance.class));
    }

    @DisplayName("deletePairsForUser 는 해당 유저가 포함된 모든 페어를 삭제한다")
    @Test
    void deletePairsForUser() {
        distanceService.deletePairsForUser(7L);
        verify(pairDistanceRepository).deleteAllByElder_IdOrGuardian_Id(7L, 7L);
    }

    @DisplayName("handleReset broadcast 는 액터의 모든 페어를 0km 로 리셋한다")
    @Test
    void handleReset_broadcast() {
        User elder = user(1L, "어르신", Role.ELDER);
        User guardian = user(2L, "보호자", Role.GUARDIAN);
        PairDistance pd1 = PairDistance.builder().elder(elder).guardian(guardian).family(family(10L)).distanceKm(5).build();
        PairDistance pd2 = PairDistance.builder().elder(elder).guardian(user(3L, "보호자2", Role.GUARDIAN))
                .family(family(10L)).distanceKm(7).build();

        given(pairDistanceRepository.findAllByUserId(1L)).willReturn(List.of(pd1, pd2));

        distanceService.handleReset(DistanceResetEvent.broadcast(1L, DistanceActionType.ATTENDANCE));

        assertThat(pd1.getDistanceKm()).isZero();
        assertThat(pd2.getDistanceKm()).isZero();
        assertThat(pd1.getLastActionType()).isEqualTo(DistanceActionType.ATTENDANCE);
    }

    @DisplayName("handleReset pair 는 (elder, guardian) 순서가 뒤바뀌어도 페어를 찾아 리셋한다")
    @Test
    void handleReset_pair_역방향() {
        User elder = user(1L, "어르신", Role.ELDER);
        User guardian = user(2L, "보호자", Role.GUARDIAN);
        PairDistance pd = PairDistance.builder().elder(elder).guardian(guardian).family(family(10L)).distanceKm(6).build();

        // actor=guardian(2), target=elder(1) — 첫 조회는 비어있고, 뒤집어서 찾음
        given(pairDistanceRepository.findByElder_IdAndGuardian_Id(2L, 1L)).willReturn(Optional.empty());
        given(pairDistanceRepository.findByElder_IdAndGuardian_Id(1L, 2L)).willReturn(Optional.of(pd));

        distanceService.handleReset(DistanceResetEvent.pair(2L, 1L, DistanceActionType.PHOTO_VIEW));

        assertThat(pd.getDistanceKm()).isZero();
        assertThat(pd.getLastActionType()).isEqualTo(DistanceActionType.PHOTO_VIEW);
    }

    @DisplayName("새로 생성되는 PairDistance 는 elder/guardian role 기준으로 올바르게 매핑된다")
    @Test
    void createPairsForApprovedMember_GUARDIAN_매핑검증() {
        Family fam = family(10L);
        User guardian = user(2L, "보호자", Role.GUARDIAN);
        User elder = user(1L, "어르신", Role.ELDER);

        FamilyMember approvedGuardian = FamilyMember.builder().family(fam).user(guardian).role(Role.GUARDIAN).build();
        FamilyMember elderMember = FamilyMember.builder().family(fam).user(elder).role(Role.ELDER).build();

        given(familyMemberRepository.findAllByFamily_IdAndRoleAndStatus(10L, Role.ELDER, MemberStatus.APPROVED))
                .willReturn(List.of(elderMember));
        given(pairDistanceRepository.existsByElder_IdAndGuardian_Id(1L, 2L)).willReturn(false);

        ArgumentCaptor<PairDistance> captor = ArgumentCaptor.forClass(PairDistance.class);

        distanceService.createPairsForApprovedMember(approvedGuardian);

        verify(pairDistanceRepository).save(captor.capture());
        PairDistance saved = captor.getValue();
        assertThat(saved.getElder().getId()).isEqualTo(1L);
        assertThat(saved.getGuardian().getId()).isEqualTo(2L);
    }
}
