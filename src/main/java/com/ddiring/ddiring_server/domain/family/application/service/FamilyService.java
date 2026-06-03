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
import com.ddiring.ddiring_server.domain.family.presentation.dto.request.JoinFamilyRequest;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.CreateFamilyResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.ElderListResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.FamilyMemberResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.FamilyStatusResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.InviteCodeResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.MemberListResponse;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.domain.family.application.event.FamilyMemberApprovedEvent;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public FamilyStatusResponse getFamilyStatus(Long userId) {
        return familyMemberRepository.findByUser_Id(userId)
                .map(fm -> FamilyStatusResponse.inFamily(fm.getStatus()))
                .orElse(FamilyStatusResponse.notInFamily());
    }

    @Transactional(readOnly = true)
    public InviteCodeResponse getInviteCode(Long userId) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyNotFoundException::new);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(FamilyNotFoundException::new);

        return InviteCodeResponse.of(family.getInviteCode());
    }

    @Transactional
    public CreateFamilyResponse createFamily(Long userId, CreateFamilyRequest request) {
        if (familyMemberRepository.existsByUser_Id(userId)) {
            throw new AlreadyInFamilyException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        String inviteCode = generateUniqueInviteCode();

        Family family = familyRepository.save(Family.builder()
                .name(request.name())
                .inviteCode(inviteCode)
                .createdBy(user)
                .build());

        familyMemberRepository.save(FamilyMember.builder()
                .family(family)
                .user(user)
                .role(Role.GUARDIAN)
                .status(MemberStatus.APPROVED)
                .joinedAt(LocalDateTime.now())
                .build());

        return CreateFamilyResponse.from(family);
    }

    @Transactional(readOnly = true)
    public MemberListResponse getMembers(Long userId) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyNotFoundException::new);

        List<FamilyMemberResponse> members = familyMemberRepository.findAllByFamily_Id(familyId).stream()
                .map(FamilyMemberResponse::from)
                .toList();

        return MemberListResponse.of(members);
    }

    @Transactional(readOnly = true)
    public ElderListResponse getConnectedElders(Long userId) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyNotFoundException::new);

        List<FamilyMemberResponse> elders = familyMemberRepository
                .findAllByFamily_IdAndRoleAndStatus(familyId, Role.ELDER, MemberStatus.APPROVED).stream()
                .map(FamilyMemberResponse::from)
                .toList();

        return ElderListResponse.of(elders);
    }

    @Transactional
    public void joinFamily(Long userId, JoinFamilyRequest request) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        if (familyMemberRepository.existsByUser_Id(userId)) {
            throw new AlreadyInFamilyException();
        }

        Family family = familyRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(FamilyNotFoundException::new);

        familyMemberRepository.save(FamilyMember.builder()
                .family(family)
                .user(user)
                .role(user.getRole())
                .status(MemberStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void approveMember(Long userId, Long memberId) {
        FamilyMember member = validateFamilyOwner(userId, memberId);
        member.approve();
        notifyApproval(member);
    }

    /**
     * 가입 승인 알림 이벤트를 발행한다. 실제 FCM 발송은 트랜잭션 커밋 이후에 수행된다.
     * LAZY 연관(user/family)은 트랜잭션이 살아 있는 지금 시점에 미리 추출한다.
     * FCM 토큰이 없으면 이벤트를 발행하지 않는다.
     */
    private void notifyApproval(FamilyMember member) {
        String token = member.getUser().getFcmToken();
        if (token == null || token.isBlank()) {
            return;
        }
        eventPublisher.publishEvent(
                new FamilyMemberApprovedEvent(token, member.getFamily().getName()));
    }

    @Transactional
    public void rejectMember(Long userId, Long memberId) {
        FamilyMember member = validateFamilyOwner(userId, memberId);
        Long targetUserId = member.getUser().getId();
        familyMemberRepository.delete(member);
    }

    private FamilyMember validateFamilyOwner(Long userId, Long memberId) {
        Long familyId = familyMemberRepository.findFamilyIdByUserId(userId)
                .orElseThrow(FamilyNotFoundException::new);

        Family family = familyRepository.findById(familyId)
                .orElseThrow(FamilyNotFoundException::new);

        if (!family.getCreatedBy().getId().equals(userId)) {
            throw new NotFamilyOwnerException();
        }

        FamilyMember targetMember = familyMemberRepository.findById(memberId)
                .orElseThrow(FamilyMemberNotFoundException::new);

        if (!targetMember.getFamily().getId().equals(familyId)) {
            throw new FamilyMemberNotFoundException();
        }

        return targetMember;
    }

    private String generateUniqueInviteCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
            for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
                sb.append(INVITE_CODE_CHARS.charAt(SECURE_RANDOM.nextInt(INVITE_CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (familyRepository.existsByInviteCode(code));
        return code;
    }
}
