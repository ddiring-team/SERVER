package com.ddiring.ddiring_server.domain.family.presentation;

import com.ddiring.ddiring_server.domain.family.application.service.FamilyService;
import com.ddiring.ddiring_server.domain.family.exception.AlreadyInFamilyException;
import com.ddiring.ddiring_server.domain.family.exception.FamilyNotFoundException;
import com.ddiring.ddiring_server.domain.family.exception.NotFamilyOwnerException;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.CreateFamilyResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.ElderListResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.FamilyMemberResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.FamilyStatusResponse;
import com.ddiring.ddiring_server.domain.family.presentation.dto.response.MemberListResponse;
import com.ddiring.ddiring_server.domain.family.domain.entity.enums.MemberStatus;
import com.ddiring.ddiring_server.domain.user.domain.entity.enums.Role;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FamilyController.class)
class FamilyControllerTest {

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
    private FamilyService familyService;

    @MockitoBean
    private TokenProvider tokenProvider;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private ObjectMapper objectMapper;

    // ────────────── GET /api/families/status ──────────────

    @DisplayName("가족방에 가입되어 있지 않으면 inFamily=false, status=null 을 반환한다")
    @Test
    void getFamilyStatus_미가입() throws Exception {
        // given
        given(familyService.getFamilyStatus(any())).willReturn(FamilyStatusResponse.notInFamily());

        // when & then
        mockMvc.perform(get("/api/families/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.inFamily").value(false))
                .andExpect(jsonPath("$.data.status").isEmpty());
    }

    @DisplayName("가족방에 가입했지만 승인 대기 중이면 inFamily=true, status=PENDING 을 반환한다")
    @Test
    void getFamilyStatus_PENDING() throws Exception {
        // given
        given(familyService.getFamilyStatus(any())).willReturn(FamilyStatusResponse.inFamily(MemberStatus.PENDING));

        // when & then
        mockMvc.perform(get("/api/families/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.inFamily").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @DisplayName("가족방 승인까지 완료되면 inFamily=true, status=APPROVED 를 반환한다")
    @Test
    void getFamilyStatus_APPROVED() throws Exception {
        // given
        given(familyService.getFamilyStatus(any())).willReturn(FamilyStatusResponse.inFamily(MemberStatus.APPROVED));

        // when & then
        mockMvc.perform(get("/api/families/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.inFamily").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    // ────────────── POST /api/families ──────────────

    @DisplayName("유효한 요청으로 가족방을 생성하면 201 CREATED 와 초대코드를 반환한다")
    @Test
    void createFamily_성공() throws Exception {
        // given
        CreateFamilyResponse response = new CreateFamilyResponse(1L, "우리 가족", "ABC123");
        given(familyService.createFamily(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "우리 가족"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.familyId").value(1))
                .andExpect(jsonPath("$.data.name").value("우리 가족"))
                .andExpect(jsonPath("$.data.inviteCode").value("ABC123"));
    }

    @DisplayName("가족방 이름 없이 요청하면 400 BAD REQUEST 를 반환한다")
    @Test
    void createFamily_실패_이름없음() throws Exception {
        mockMvc.perform(post("/api/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));
    }

    @DisplayName("이미 가족방에 소속된 경우 409 CONFLICT 를 반환한다")
    @Test
    void createFamily_실패_이미가족방소속() throws Exception {
        // given
        given(familyService.createFamily(any(), any())).willThrow(new AlreadyInFamilyException());

        // when & then
        mockMvc.perform(post("/api/families")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", "우리 가족"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(409));
    }

    // ────────────── GET /api/families/members ──────────────

    @DisplayName("구성원 전체 목록 조회 시 200 OK 와 구성원 목록을 반환한다")
    @Test
    void getMembers_성공() throws Exception {
        // given
        FamilyMemberResponse member = new FamilyMemberResponse(
                1L, 2L, "보호자", Role.GUARDIAN, MemberStatus.APPROVED, LocalDateTime.now());
        given(familyService.getMembers(any())).willReturn(MemberListResponse.of(List.of(member)));

        // when & then
        mockMvc.perform(get("/api/families/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.members").isArray())
                .andExpect(jsonPath("$.data.members[0].name").value("보호자"))
                .andExpect(jsonPath("$.data.members[0].role").value("GUARDIAN"));
    }

    @DisplayName("소속된 가족방이 없을 때 구성원 조회 시 404 NOT FOUND 를 반환한다")
    @Test
    void getMembers_실패_가족방없음() throws Exception {
        // given
        given(familyService.getMembers(any())).willThrow(new FamilyNotFoundException());

        // when & then
        mockMvc.perform(get("/api/families/members"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404));
    }

    // ────────────── GET /api/families/elders ──────────────

    @DisplayName("연결된 어르신 목록 조회 시 200 OK 와 어르신 목록을 반환한다")
    @Test
    void getConnectedElders_성공() throws Exception {
        // given
        FamilyMemberResponse elder = new FamilyMemberResponse(
                2L, 3L, "어르신", Role.ELDER, MemberStatus.APPROVED, LocalDateTime.now());
        given(familyService.getConnectedElders(any())).willReturn(ElderListResponse.of(List.of(elder)));

        // when & then
        mockMvc.perform(get("/api/families/elders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.elders").isArray())
                .andExpect(jsonPath("$.data.elders[0].name").value("어르신"))
                .andExpect(jsonPath("$.data.elders[0].role").value("ELDER"));
    }

    // ────────────── PATCH /api/families/members/{memberId}/approve ──────────────

    @DisplayName("구성원 승인 요청 시 200 OK 를 반환한다")
    @Test
    void approveMember_성공() throws Exception {
        // given
        willDoNothing().given(familyService).approveMember(any(), any());

        // when & then
        mockMvc.perform(patch("/api/families/members/5/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200));
    }

    @DisplayName("방 생성자가 아닌 유저가 승인을 시도하면 403 FORBIDDEN 을 반환한다")
    @Test
    void approveMember_실패_방생성자아님() throws Exception {
        // given
        willThrow(new NotFamilyOwnerException()).given(familyService).approveMember(any(), any());

        // when & then
        mockMvc.perform(patch("/api/families/members/5/approve"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403));
    }

    // ────────────── DELETE /api/families/members/{memberId} ──────────────

    @DisplayName("구성원 거절 요청 시 200 OK 를 반환한다")
    @Test
    void rejectMember_성공() throws Exception {
        // given
        willDoNothing().given(familyService).rejectMember(any(), any());

        // when & then
        mockMvc.perform(delete("/api/families/members/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200));
    }

    @DisplayName("방 생성자가 아닌 유저가 거절을 시도하면 403 FORBIDDEN 을 반환한다")
    @Test
    void rejectMember_실패_방생성자아님() throws Exception {
        // given
        willThrow(new NotFamilyOwnerException()).given(familyService).rejectMember(any(), any());

        // when & then
        mockMvc.perform(delete("/api/families/members/5"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403));
    }
}
