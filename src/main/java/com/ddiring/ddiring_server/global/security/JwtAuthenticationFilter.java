package com.ddiring.ddiring_server.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
// http 요청에서 jwt 토큰 추출하고, 인증 정보를 설정하는 필터 클래스
// 이 필터는 http 요청마다 실행됨.
// 요청에 포함된 jwt 를 검증해서, security context 에 인증 정볼르 설정하는 역할
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    // OPTIONS 요청은 필터를 건너뛰도로록 설정(cors 사전 요청 등 무시 = cors preflight )
    // 클라이언트가 실제 요청 보내기 전에 options 요청 보내서 서버가 이 요청을 확인하는 과정인데
    // 이 요청에 대해서는 필터를 적용할 필요가 없겠지!
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // OPTIONS 메서드는 필터 대상 아님
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // /auth/** 경로는 필터를 건너뜀 (인증 불필요)
        if (path.startsWith("/auth/")) {
            return true;
        }

        return false; // false 반환시 필터 동작 o
    }

    /**
     * 필터 내부 로직
     * 요청에서 jwt 파싱 -> 검증 -> 인증객체 생성 -> SecurityContext 설정
     * 즉, 클라가 보낸 토큰이 정상이면, 해당 사용자를 인증된 상태로 만들어줌
     * 토큰이 유효하다면, 토큰에 담긴 사용자 id 바탕으로 스프링 시큐리티의 인증 객체 생성
     * 이 인증정보를 security context 에 담음
     * 다음 filter 로 넘긴다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            // Authorization Header에서 Bearer 토큰 추출
            String token = parseBearerToken(request);

            if (token != null && !token.isEmpty() && !token.equalsIgnoreCase("null")) {
                String userId = tokenProvider.validateAndGetUserId(token);
                log.info("Authenticated user ID : {}", userId);

                // String을 Long으로 변환
                Long userIdLong = Long.parseLong(userId);

                // 사용자 id 기반 인증 객체 생성 (Long 타입으로 저장)
                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userIdLong, null,
                        AuthorityUtils.NO_AUTHORITIES);

                // 인증객체에 세부 정보 추가함 (ip , 세션..)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // securitycontext 생성 및 인증 객체 설정
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);

                // 현재 쓰레드에 securitycontext 등록
                SecurityContextHolder.setContext(securityContext);

                // 이렇게 인증이 완료되면... 이후 컨트롤러나 서비스에서는
                // @AuthenticationPrincipal 또는 SucurityContextHolder 를 통해서 현재 로그인한
                // 사용자 정보에 접근 가능

            }

        }catch (Exception ex){
            log.error("could not set user authentication in security context", ex);
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization Header에서 Bearer 토큰 추출
     */
    private String parseBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

}
