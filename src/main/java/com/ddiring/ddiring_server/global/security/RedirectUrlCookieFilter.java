package com.ddiring.ddiring_server.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
                Cookie cookie = new Cookie(REDIRECT_URI_COOKIE_NAME, redirectUrl);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(COOKIE_MAX_AGE);
                response.addCookie(cookie);
            }
        }
        filterChain.doFilter(request, response);
    }
}
