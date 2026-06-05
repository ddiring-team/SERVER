package com.ddiring.ddiring_server.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RedirectUrlCookieFilter extends OncePerRequestFilter {

    private static final String REDIRECT_URI_PARAM = "redirect_url";
    private static final String REDIRECT_URI_COOKIE_NAME = "redirect_url";
    private static final int COOKIE_MAX_AGE = 180;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/oauth2/authorization")) {
            String redirectUrl = request.getParameter(REDIRECT_URI_PARAM);
            if (StringUtils.hasText(redirectUrl)) {
                // 카카오 콜백(cross-site)에서도 쿠키가 실려 오도록 SameSite=None; Secure 로 내린다.
                ResponseCookie cookie = ResponseCookie.from(REDIRECT_URI_COOKIE_NAME, redirectUrl)
                        .path("/")
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("None")
                        .maxAge(COOKIE_MAX_AGE)
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            }
        }
        filterChain.doFilter(request, response);
    }
}
