package com.ddiring.ddiring_server.domain.auth.presentation;

import com.ddiring.ddiring_server.domain.auth.application.service.AuthService;
import com.ddiring.ddiring_server.domain.auth.exception.DuplicateLoginIdException;
import com.ddiring.ddiring_server.domain.auth.exception.ElderAlreadyRegisteredException;
import com.ddiring.ddiring_server.domain.auth.exception.ElderNotFoundException;
import com.ddiring.ddiring_server.domain.auth.exception.InvalidCredentialsException;
import com.ddiring.ddiring_server.domain.auth.presentation.dto.response.AuthResponse;
import com.ddiring.ddiring_server.domain.family.exception.FamilyNotFoundException;
import com.ddiring.ddiring_server.global.security.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        @Order(1)
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.securityMatcher("/**")
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    // ────────────── POST /api/auth/guardian/signup ──────────────

    @DisplayName("유효한 정보로 보호자 회원가입을 하면 201 CREATED 와 토큰을 반환한다")
    @Test
    void guardianSignup_성공() throws Exception {
        // given
        given(authService.guardianSignup(any())).willReturn(new AuthResponse("jwt-token", "GUARDIAN"));

        // when & then
        mockMvc.perform(post("/api/auth/guardian/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("loginId", "guardian01", "password", "password123", "name", "홍길동", "phone", "01012345678", "birthDate", "1990-01-15"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.role").value("GUARDIAN"));
    }

    @DisplayName("아이디가 너무 짧으면 400 BAD REQUEST 를 반환한다")
    @Test
    void guardianSignup_실패_아이디길이부족() throws Exception {
        mockMvc.perform(post("/api/auth/guardian/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("loginId", "abc", "password", "password123", "name", "홍길동", "phone", "01012345678", "birthDate", "1990-01-15"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));
    }

    @DisplayName("비밀번호가 너무 짧으면 400 BAD REQUEST 를 반환한다")
    @Test
    void guardianSignup_실패_비밀번호길이부족() throws Exception {
        mockMvc.perform(post("/api/auth/guardian/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("loginId", "guardian01", "password", "short", "name", "홍길동", "phone", "01012345678", "birthDate", "1990-01-15"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));
    }

    @DisplayName("중복 아이디로 회원가입을 하면 409 CONFLICT 를 반환한다")
    @Test
    void guardianSignup_실패_중복아이디() throws Exception {
        // given
        given(authService.guardianSignup(any())).willThrow(new DuplicateLoginIdException());

        // when & then
        mockMvc.perform(post("/api/auth/guardian/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("loginId", "guardian01", "password", "password123", "name", "홍길동", "phone", "01012345678", "birthDate", "1990-01-15"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(409));
    }

    // ────────────── POST /api/auth/guardian/login ──────────────

    @DisplayName("올바른 자격증명으로 보호자 로그인을 하면 200 OK 와 토큰을 반환한다")
    @Test
    void guardianLogin_성공() throws Exception {
        // given
        given(authService.guardianLogin(any())).willReturn(new AuthResponse("jwt-token", "GUARDIAN"));

        // when & then
        mockMvc.perform(post("/api/auth/guardian/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("loginId", "guardian01", "password", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.role").value("GUARDIAN"));
    }

    @DisplayName("잘못된 자격증명으로 로그인하면 401 UNAUTHORIZED 를 반환한다")
    @Test
    void guardianLogin_실패_인증실패() throws Exception {
        // given
        given(authService.guardianLogin(any())).willThrow(new InvalidCredentialsException());

        // when & then
        mockMvc.perform(post("/api/auth/guardian/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("loginId", "guardian01", "password", "wrongpw"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }

    // ────────────── POST /api/auth/elder/register ──────────────

    @DisplayName("유효한 초대코드로 어르신 최초 등록을 하면 201 CREATED 와 토큰을 반환한다")
    @Test
    void elderRegister_성공() throws Exception {
        // given
        given(authService.elderRegister(any())).willReturn(new AuthResponse("elder-token", "ELDER"));

        // when & then
        mockMvc.perform(post("/api/auth/elder/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("inviteCode", "ABC123", "name", "김어르신", "phone", "01098765432"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.token").value("elder-token"))
                .andExpect(jsonPath("$.data.role").value("ELDER"));
    }

    @DisplayName("존재하지 않는 초대코드로 등록하면 404 NOT FOUND 를 반환한다")
    @Test
    void elderRegister_실패_초대코드없음() throws Exception {
        // given
        given(authService.elderRegister(any())).willThrow(new FamilyNotFoundException());

        // when & then
        mockMvc.perform(post("/api/auth/elder/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("inviteCode", "XXXXXX", "name", "김어르신", "phone", "01098765432"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404));
    }

    @DisplayName("같은 가족방에 동일 이름으로 재등록하면 409 CONFLICT 를 반환한다")
    @Test
    void elderRegister_실패_중복이름() throws Exception {
        // given
        given(authService.elderRegister(any())).willThrow(new ElderAlreadyRegisteredException());

        // when & then
        mockMvc.perform(post("/api/auth/elder/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("inviteCode", "ABC123", "name", "김어르신", "phone", "01098765432"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(409));
    }

    // ────────────── POST /api/auth/elder/login ──────────────

    @DisplayName("올바른 초대코드와 이름으로 어르신 로그인을 하면 200 OK 와 토큰을 반환한다")
    @Test
    void elderLogin_성공() throws Exception {
        // given
        given(authService.elderLogin(any())).willReturn(new AuthResponse("elder-token", "ELDER"));

        // when & then
        mockMvc.perform(post("/api/auth/elder/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("inviteCode", "ABC123", "name", "김어르신"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").value("elder-token"))
                .andExpect(jsonPath("$.data.role").value("ELDER"));
    }

    @DisplayName("가족방에 없는 정보로 어르신 로그인을 하면 404 NOT FOUND 를 반환한다")
    @Test
    void elderLogin_실패_정보없음() throws Exception {
        // given
        given(authService.elderLogin(any())).willThrow(new ElderNotFoundException());

        // when & then
        mockMvc.perform(post("/api/auth/elder/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("inviteCode", "ABC123", "name", "없는사람"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404));
    }
}
