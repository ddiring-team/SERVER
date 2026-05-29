package com.ddiring.ddiring_server.domain.auth.application.service;

import com.ddiring.ddiring_server.domain.auth.exception.*;
import com.ddiring.ddiring_server.domain.auth.presentation.dto.request.*;
import com.ddiring.ddiring_server.domain.user.exception.UserNotFoundException;
import com.ddiring.ddiring_server.domain.auth.presentation.dto.response.AuthResponse;
import com.ddiring.ddiring_server.domain.family.domain.entity.Family;
import com.ddiring.ddiring_server.domain.family.domain.entity.FamilyMember;
import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyMemberRepository;
import com.ddiring.ddiring_server.domain.family.domain.repository.FamilyRepository;
import com.ddiring.ddiring_server.domain.family.exception.FamilyNotFoundException;
import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse guardianSignup(GuardianSignupRequest request) {
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new DuplicateLoginIdException();
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicatePhoneException();
        }

        User user = userRepository.save(User.builder()
                .loginId(request.loginId())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .role(Role.GUARDIAN)
                .build());

        return AuthResponse.of(tokenProvider.create(user), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse guardianLogin(GuardianLoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return AuthResponse.of(tokenProvider.create(user), user.getRole().name());
    }

    @Transactional
    public AuthResponse elderRegister(ElderRegisterRequest request) {
        Family family = familyRepository.findByInviteCode(request.inviteCode())
                .orElseThrow(FamilyNotFoundException::new);

        if (familyMemberRepository.findElderByInviteCodeAndName(request.inviteCode(), request.name()).isPresent()) {
            throw new ElderAlreadyRegisteredException();
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicatePhoneException();
        }

        User elder = userRepository.save(User.builder()
                .name(request.name())
                .phone(request.phone())
                .birthDate(request.birthDate())
                .role(Role.ELDER)
                .build());

        familyMemberRepository.save(FamilyMember.builder()
                .family(family)
                .user(elder)
                .role(Role.ELDER)
                .status(MemberStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .build());

        return AuthResponse.of(tokenProvider.create(elder), elder.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse elderLogin(ElderLoginRequest request) {
        FamilyMember member = familyMemberRepository
                .findElderByInviteCodeAndName(request.inviteCode(), request.name())
                .orElseThrow(ElderNotFoundException::new);

        return AuthResponse.of(tokenProvider.create(member.getUser()), member.getUser().getRole().name());
    }

    @Transactional
    public AuthResponse completeGuardianProfile(Long userId, KakaoGuardianCompleteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicatePhoneException();
        }

        user.completeProfile(request.name(), request.phone(), request.birthDate(), Role.GUARDIAN);

        return AuthResponse.of(tokenProvider.create(user), Role.GUARDIAN.name());
    }

    @Transactional
    public AuthResponse completeElderProfile(Long userId, KakaoElderCompleteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (userRepository.existsByPhone(request.phone())) {
            throw new DuplicatePhoneException();
        }

        user.completeProfile(request.name(), request.phone(), request.birthDate(), Role.ELDER);

        return AuthResponse.of(tokenProvider.create(user), Role.ELDER.name());
    }
}
