package com.ddiring.ddiring_server.global.security;

import com.ddiring.ddiring_server.global.security.vo.CustomUser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String REDIRECT_URI_COOKIE_NAME = "redirect_url";
    private static final String DEFAULT_REDIRECT_URI = "ddiring://auth";

    private final TokenProvider tokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUser customUser = (CustomUser) authentication.getPrincipal();

        String token = tokenProvider.createByUserId(customUser.getUserId());
        String redirectUri = extractRedirectUri(request).orElse(DEFAULT_REDIRECT_URI);

        String targetUrl = redirectUri
                + "?token=" + token
                + "&isNewUser=" + customUser.isNewUser();

        log.info("OAuth2 login success, userId={}, isNewUser={}", customUser.getUserId(), customUser.isNewUser());

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
