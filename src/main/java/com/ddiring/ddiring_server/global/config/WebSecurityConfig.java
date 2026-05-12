package com.ddiring.ddiring_server.global.config;

import com.ddiring.ddiring_server.global.security.HttpCookieOAuth2AuthorizationRequestRepository;
import com.ddiring.ddiring_server.global.security.JwtAuthenticationFilter;
import com.ddiring.ddiring_server.global.security.OAuthSuccessHandler;
import com.ddiring.ddiring_server.global.security.RedirectUrlCookieFilter;
import com.ddiring.ddiring_server.global.security.application.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuthSuccessHandler oAuthSuccessHandler;
    private final RedirectUrlCookieFilter redirectUrlCookieFilter;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    // 보안 필터의 동작 방식을 정의.
    // 보안 필터 체인 정의 : 인증, 인가, 세션, 예외처리, jwt 필터 설정
    // 우리가 원하는대로 보안 정책을 커스터마이징
    // ex. 어떤 url 은 인증 없이 접근가능하게 할, 어떤 경로는 로그인 후에만 접근 가능하게 하지..
    // 우리가 만든 jwt 필터를 어디에 적용할지 선택 가능.
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // cors 설정 활성화 (프론트, 백엔드가 다른 도메인에 있을때 cors 허용
                // corsConfiguratinSource() 를 통 허용 도메인, 헤더 ,메서드 등을 지정 가능!
                .csrf(csrf -> csrf.disable()) // csrf 비활성화
                // jwt 기반 rest api 에서는 보통 세션, 쿠키가 필요없어서 csrf 보호가 필요없다.
                .httpBasic(httpBasic -> httpBasic.disable())
                // 기본 인증 방식 비활성화 . basic 인증은 id 와 비번을 매 요청마다 헤더에 실어서 보내는 방식인데,
                // 우리는 jwt 기반 사용하기 때문에 꺼준다.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 세션 사용 안함 (jwt 기반이기 때문에) . 매우 중요
                .authorizeHttpRequests(auth-> auth
                        .requestMatchers(
                                "/",
                                "/oauth2/**",
                                "/login/**",
                                "/api/s3/presigned-url",
                                "api/users",
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository))
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuthSuccessHandler))
                .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(redirectUrlCookieFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                );
        return http.build();
    }

    // cors 설정을 정의하는 bean
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true); // 자격 증명 포함 허용 (예 : 쿠키, authorization 헤더) - true 로 설정해야
        // 클라가 인증정보를 함께 보낼 수 있다.
        configuration.setAllowedOrigins(
                Arrays.asList(
                        "http://localhost:3000",
                        "https://ddiringapp.com",
                        "https://www.ddiringapp.com",
                        "https://api.ddiringapp.com"
                )
        );
        // 예를 들어, 리액트 프론트엔드가 http://localhost:3000 에서 실행된다면, 그 주소를 명시해야겟지
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")); // 허용할 메소드
        configuration.setAllowedHeaders(List.of("*")); // 모든 요청 헤더 허용
        configuration.setExposedHeaders(List.of("*")); // 응답 헤더 노출 - 응답시 브라우저에서 접근 가능할 수 있도록 허용할 헤더를 지정.
        // 위의 CORS 설정을 모든 경로에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 요청에 대해 설정 적용
        return source;
    }
}
