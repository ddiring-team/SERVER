package com.ddiring.ddiring_server.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

/**
 * OAuth2 로그인 실패 핸들러.
 * 기본 동작은 {@code /login?error}로 리다이렉트하는데, 이 프로젝트엔 {@code /login} 매핑이 없어 404가 떴고
 * 딥링크가 터지지 않아 앱이 실패를 인지하지 못했다.
 * 여기서 실제 실패 원인을 로그로 남기고, 앱 딥링크({@code redirect_url})로 error 파라미터를 돌려보낸다.
 */
@Slf4j
@Component
public class OAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String REDIRECT_URI_COOKIE_NAME = "redirect_url";
    private static final String DEFAULT_REDIRECT_URI = "ddiring://auth";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorCode = "authentication_failed";
        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            OAuth2Error error = oauthEx.getError();
            errorCode = error.getErrorCode();
            log.warn("OAuth2 login failure: code={}, description={}, uri={}",
                    error.getErrorCode(), error.getDescription(), error.getUri());
        } else {
            log.warn("OAuth2 login failure: {}", exception.getMessage(), exception);
        }

        boolean hasAuthRequestCookie = request.getCookies() != null
                && Arrays.stream(request.getCookies())
                .anyMatch(c -> "oauth2_auth_request".equals(c.getName()));
        log.warn("OAuth2 login failure context: oauth2_auth_request cookie present={}", hasAuthRequestCookie);

        String redirectUri = extractRedirectUri(request).orElse(DEFAULT_REDIRECT_URI);
        String targetUrl = redirectUri
                + "?error=" + URLEncoder.encode(errorCode, StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private Optional<String> extractRedirectUri(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> REDIRECT_URI_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::hasText)
                .findFirst();
    }
}
