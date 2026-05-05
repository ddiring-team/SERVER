package com.ddiring.ddiring_server.domain.family.application.service;

import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import com.ddiring.ddiring_server.domain.family.domain.entity.FamilyMember;
import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyRepository;
import com.ddiring.ddiring_server.domain.family.exception.AlreadyInFamilyException;
import com.ddiring.ddiring_server.domain.family.exception.FamilyMemberNotFoundException;
import com.ddiring.ddiring_server.domain.family.exception.FamilyNotFoundException;
import com.ddiring.ddiring_server.domain.family.exception.NotFamilyOwnerException;
import com.ddiring.ddiring_server.domain.family.presentation.dto.request.CreateFamilyRequest;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.CreateFamilyResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.ElderListResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.MemberListResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FamilyServiceTest {

    @Mock
    private FamilyRepository familyRepository;

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FamilyService familyService;

    private final Long userId = 1L;
    private final Long familyId = 10L;
    private final Long memberId = 5L;

    // ────────────── createFamily ──────────────

    @DisplayName("가족방에 소속되지 않은 유저가 방을 생성하면 초대코드가 포함된 응답을 반환한다")
    @Test
    void createFamily_성공() {
        // given
        User user = User.builder().name("보호자").role(Role.GUARDIAN).build();
        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(user).build();

        given(familyMemberRepository.existsByUser_Id(userId)).willReturn(false);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(familyRepository.existsByInviteCode(any())).willReturn(false);
        given(familyRepository.save(any(Family.class))).willReturn(family);

        // when
        CreateFamilyResponse response = familyService.createFamily(userId, new CreateFamilyRequest("우리 가족"));

        // then
        assertThat(response.name()).isEqualTo("우리 가족");
        assertThat(response.inviteCode()).isEqualTo("ABC123");
        verify(familyRepository).save(any(Family.class));
        verify(familyMemberRepository).save(any(FamilyMember.class));
    }

    @DisplayName("이미 가족방에 소속된 유저가 방을 생성하면 AlreadyInFamilyException 이 발생한다")
    @Test
    void createFamily_실패_이미가족방소속() {
        // given
        given(familyMemberRepository.existsByUser_Id(userId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> familyService.createFamily(userId, new CreateFamilyRequest("우리 가족")))
                .isInstanceOf(AlreadyInFamilyException.class);
    }

    // ────────────── getMembers ──────────────

    @DisplayName("가족방에 소속된 유저가 전체 구성원을 조회하면 PENDING 포함 전원을 반환한다")
    @Test
    void getMembers_성공() {
        // given
        User user = User.builder().name("보호자").role(Role.GUARDIAN).build();
        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(user).build();
        FamilyMember member = FamilyMember.builder()
                .family(family).user(user).role(Role.GUARDIAN).status(MemberStatus.APPROVED).build();

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(familyMemberRepository.findAllByFamily_Id(familyId)).willReturn(List.of(member));

        // when
        MemberListResponse response = familyService.getMembers(userId);

        // then
        assertThat(response.members()).hasSize(1);
        assertThat(response.members().get(0).name()).isEqualTo("보호자");
        assertThat(response.members().get(0).role()).isEqualTo(Role.GUARDIAN);
    }

    @DisplayName("가족방에 소속되지 않은 유저가 구성원을 조회하면 FamilyNotFoundException 이 발생한다")
    @Test
    void getMembers_실패_가족방없음() {
        // given
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> familyService.getMembers(userId))
                .isInstanceOf(FamilyNotFoundException.class);
    }

    // ────────────── getConnectedElders ──────────────

    @DisplayName("연결된 어르신 조회 시 APPROVED 상태의 ELDER 역할 구성원만 반환한다")
    @Test
    void getConnectedElders_성공() {
        // given
        User elderUser = User.builder().name("어르신").role(Role.ELDER).build();
        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(elderUser).build();
        FamilyMember elder = FamilyMember.builder()
                .family(family).user(elderUser).role(Role.ELDER).status(MemberStatus.APPROVED).build();

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(familyMemberRepository.findAllByFamily_IdAndRoleAndStatus(familyId, Role.ELDER, MemberStatus.APPROVED))
                .willReturn(List.of(elder));

        // when
        ElderListResponse response = familyService.getConnectedElders(userId);

        // then
        assertThat(response.elders()).hasSize(1);
        assertThat(response.elders().get(0).role()).isEqualTo(Role.ELDER);
        assertThat(response.elders().get(0).status()).isEqualTo(MemberStatus.APPROVED);
    }

    @DisplayName("가족방에 소속되지 않은 유저가 어르신을 조회하면 FamilyNotFoundException 이 발생한다")
    @Test
    void getConnectedElders_실패_가족방없음() {
        // given
        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> familyService.getConnectedElders(userId))
                .isInstanceOf(FamilyNotFoundException.class);
    }

    // ────────────── approveMember ──────────────

    @DisplayName("방 생성자가 대기 중인 구성원을 승인하면 APPROVED 상태로 변경된다")
    @Test
    void approveMember_성공() {
        // given
        User creator = User.builder().build();
        ReflectionTestUtils.setField(creator, "id", userId);

        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(creator).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        FamilyMember targetMember = FamilyMember.builder()
                .family(family).user(creator).role(Role.ELDER).build();

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));
        given(familyMemberRepository.findById(memberId)).willReturn(Optional.of(targetMember));

        // when
        familyService.approveMember(userId, memberId);

        // then
        assertThat(targetMember.getStatus()).isEqualTo(MemberStatus.APPROVED);
        assertThat(targetMember.getJoinedAt()).isNotNull();
    }

    @DisplayName("방 생성자가 아닌 유저가 구성원을 승인하려 하면 NotFamilyOwnerException 이 발생한다")
    @Test
    void approveMember_실패_방생성자아님() {
        // given
        User otherUser = User.builder().build();
        ReflectionTestUtils.setField(otherUser, "id", 999L);

        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(otherUser).build();

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));

        // when & then
        assertThatThrownBy(() -> familyService.approveMember(userId, memberId))
                .isInstanceOf(NotFamilyOwnerException.class);
    }

    @DisplayName("존재하지 않는 구성원을 승인하려 하면 FamilyMemberNotFoundException 이 발생한다")
    @Test
    void approveMember_실패_구성원없음() {
        // given
        User creator = User.builder().build();
        ReflectionTestUtils.setField(creator, "id", userId);

        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(creator).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));
        given(familyMemberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> familyService.approveMember(userId, memberId))
                .isInstanceOf(FamilyMemberNotFoundException.class);
    }

    // ────────────── rejectMember ──────────────

    @DisplayName("방 생성자가 구성원을 거절하면 해당 구성원 레코드가 삭제된다")
    @Test
    void rejectMember_성공() {
        // given
        User creator = User.builder().build();
        ReflectionTestUtils.setField(creator, "id", userId);

        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(creator).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        FamilyMember targetMember = FamilyMember.builder()
                .family(family).user(creator).role(Role.ELDER).build();

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));
        given(familyMemberRepository.findById(memberId)).willReturn(Optional.of(targetMember));

        // when
        familyService.rejectMember(userId, memberId);

        // then
        verify(familyMemberRepository).delete(targetMember);
    }

    @DisplayName("방 생성자가 아닌 유저가 구성원을 거절하려 하면 NotFamilyOwnerException 이 발생한다")
    @Test
    void rejectMember_실패_방생성자아님() {
        // given
        User otherUser = User.builder().build();
        ReflectionTestUtils.setField(otherUser, "id", 999L);

        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(otherUser).build();

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));

        // when & then
        assertThatThrownBy(() -> familyService.rejectMember(userId, memberId))
                .isInstanceOf(NotFamilyOwnerException.class);
    }

    @DisplayName("존재하지 않는 구성원을 거절하려 하면 FamilyMemberNotFoundException 이 발생한다")
    @Test
    void rejectMember_실패_구성원없음() {
        // given
        User creator = User.builder().build();
        ReflectionTestUtils.setField(creator, "id", userId);

        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(creator).build();
        ReflectionTestUtils.setField(family, "id", familyId);

        given(familyMemberRepository.findFamilyIdByUserId(userId)).willReturn(Optional.of(familyId));
        given(familyRepository.findById(familyId)).willReturn(Optional.of(family));
        given(familyMemberRepository.findById(memberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> familyService.rejectMember(userId, memberId))
                .isInstanceOf(FamilyMemberNotFoundException.class);
    }
}
