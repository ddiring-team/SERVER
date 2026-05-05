package com.ddiring.ddiring_server.domain.auth.application.service;

import com.ddiring.ddiring_server.domain.auth.exception.DuplicateLoginIdException;
import com.ddiring.ddiring_server.domain.auth.exception.ElderAlreadyRegisteredException;
import com.ddiring.ddiring_server.domain.auth.exception.ElderNotFoundException;
import com.ddiring.ddiring_server.domain.auth.exception.InvalidCredentialsException;
import com.ddiring.ddiring_server.domain.auth.presentation.dto.request.*;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FamilyRepository familyRepository;
    @Mock private FamilyMemberRepository familyMemberRepository;
    @Mock private TokenProvider tokenProvider;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // ────────────── guardianSignup ──────────────

    @DisplayName("중복되지 않은 아이디로 보호자 회원가입을 하면 토큰을 반환한다")
    @Test
    void guardianSignup_성공() {
        // given
        GuardianSignupRequest request = new GuardianSignupRequest("guardian01", "password123", "홍길동", "01012345678");
        User savedUser = User.builder().loginId("guardian01").name("홍길동").role(Role.GUARDIAN).build();

        given(userRepository.existsByLoginId("guardian01")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("hashed");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(tokenProvider.create(savedUser)).willReturn("jwt-token");

        // when
        AuthResponse response = authService.guardianSignup(request);

        // then
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo("GUARDIAN");
        verify(userRepository).save(any(User.class));
    }

    @DisplayName("이미 사용 중인 아이디로 회원가입을 하면 DuplicateLoginIdException 이 발생한다")
    @Test
    void guardianSignup_실패_중복아이디() {
        // given
        given(userRepository.existsByLoginId("guardian01")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.guardianSignup(
                new GuardianSignupRequest("guardian01", "password123", "홍길동", "01012345678")))
                .isInstanceOf(DuplicateLoginIdException.class);
    }

    // ────────────── guardianLogin ──────────────

    @DisplayName("올바른 아이디와 비밀번호로 로그인하면 토큰을 반환한다")
    @Test
    void guardianLogin_성공() {
        // given
        User user = User.builder().loginId("guardian01").password("hashed").role(Role.GUARDIAN).build();

        given(userRepository.findByLoginId("guardian01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "hashed")).willReturn(true);
        given(tokenProvider.create(user)).willReturn("jwt-token");

        // when
        AuthResponse response = authService.guardianLogin(new GuardianLoginRequest("guardian01", "password123"));

        // then
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo("GUARDIAN");
    }

    @DisplayName("존재하지 않는 아이디로 로그인하면 InvalidCredentialsException 이 발생한다")
    @Test
    void guardianLogin_실패_아이디없음() {
        // given
        given(userRepository.findByLoginId("notexist")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.guardianLogin(
                new GuardianLoginRequest("notexist", "password123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @DisplayName("비밀번호가 틀리면 InvalidCredentialsException 이 발생한다")
    @Test
    void guardianLogin_실패_비밀번호틀림() {
        // given
        User user = User.builder().loginId("guardian01").password("hashed").role(Role.GUARDIAN).build();

        given(userRepository.findByLoginId("guardian01")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpw", "hashed")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.guardianLogin(
                new GuardianLoginRequest("guardian01", "wrongpw")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // ────────────── elderRegister ──────────────

    @DisplayName("유효한 초대코드로 어르신 등록을 하면 PENDING 상태로 가입되고 토큰을 반환한다")
    @Test
    void elderRegister_성공() {
        // given
        ElderRegisterRequest request = new ElderRegisterRequest("ABC123", "김어르신", "01098765432", null);
        User guardian = User.builder().name("보호자").role(Role.GUARDIAN).build();
        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(guardian).build();
        User savedElder = User.builder().name("김어르신").role(Role.ELDER).build();

        given(familyRepository.findByInviteCode("ABC123")).willReturn(Optional.of(family));
        given(familyMemberRepository.findElderByInviteCodeAndName("ABC123", "김어르신"))
                .willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(savedElder);
        given(tokenProvider.create(savedElder)).willReturn("elder-token");

        // when
        AuthResponse response = authService.elderRegister(request);

        // then
        assertThat(response.token()).isEqualTo("elder-token");
        assertThat(response.role()).isEqualTo("ELDER");
        verify(familyMemberRepository).save(any(FamilyMember.class));
    }

    @DisplayName("존재하지 않는 초대코드로 어르신 등록을 하면 FamilyNotFoundException 이 발생한다")
    @Test
    void elderRegister_실패_초대코드없음() {
        // given
        given(familyRepository.findByInviteCode("XXXXXX")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.elderRegister(
                new ElderRegisterRequest("XXXXXX", "김어르신", "01098765432", null)))
                .isInstanceOf(FamilyNotFoundException.class);
    }

    @DisplayName("같은 가족방에 동일 이름으로 재등록하면 ElderAlreadyRegisteredException 이 발생한다")
    @Test
    void elderRegister_실패_중복이름() {
        // given
        User guardian = User.builder().name("보호자").role(Role.GUARDIAN).build();
        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(guardian).build();
        FamilyMember existing = FamilyMember.builder()
                .family(family).user(User.builder().name("김어르신").role(Role.ELDER).build())
                .role(Role.ELDER).status(MemberStatus.PENDING).build();

        given(familyRepository.findByInviteCode("ABC123")).willReturn(Optional.of(family));
        given(familyMemberRepository.findElderByInviteCodeAndName("ABC123", "김어르신"))
                .willReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> authService.elderRegister(
                new ElderRegisterRequest("ABC123", "김어르신", "01098765432", null)))
                .isInstanceOf(ElderAlreadyRegisteredException.class);
    }

    // ────────────── elderLogin ──────────────

    @DisplayName("올바른 초대코드와 이름으로 어르신 로그인을 하면 토큰을 반환한다")
    @Test
    void elderLogin_성공() {
        // given
        User elderUser = User.builder().name("김어르신").role(Role.ELDER).build();
        User guardian = User.builder().name("보호자").role(Role.GUARDIAN).build();
        Family family = Family.builder().name("우리 가족").inviteCode("ABC123").createdBy(guardian).build();
        FamilyMember member = FamilyMember.builder()
                .family(family).user(elderUser).role(Role.ELDER).status(MemberStatus.PENDING).build();

        given(familyMemberRepository.findElderByInviteCodeAndName("ABC123", "김어르신"))
                .willReturn(Optional.of(member));
        given(tokenProvider.create(elderUser)).willReturn("elder-token");

        // when
        AuthResponse response = authService.elderLogin(new ElderLoginRequest("ABC123", "김어르신"));

        // then
        assertThat(response.token()).isEqualTo("elder-token");
        assertThat(response.role()).isEqualTo("ELDER");
    }

    @DisplayName("가족방에 없는 이름으로 어르신 로그인을 하면 ElderNotFoundException 이 발생한다")
    @Test
    void elderLogin_실패_정보없음() {
        // given
        given(familyMemberRepository.findElderByInviteCodeAndName("ABC123", "없는사람"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.elderLogin(
                new ElderLoginRequest("ABC123", "없는사람")))
                .isInstanceOf(ElderNotFoundException.class);
    }
}
