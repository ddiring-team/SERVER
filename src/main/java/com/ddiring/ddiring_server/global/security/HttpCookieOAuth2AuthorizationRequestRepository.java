package com.ddiring.ddiring_server.global.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String OAUTH2_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_MAX_AGE = 180;

    private final ObjectMapper objectMapper;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookie(request, OAUTH2_REQUEST_COOKIE_NAME)
                .map(cookie -> deserialize(cookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response, OAUTH2_REQUEST_COOKIE_NAME);
            return;
        }
        Cookie cookie = new Cookie(OAUTH2_REQUEST_COOKIE_NAME, serialize(authorizationRequest));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                  HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(request, response, OAUTH2_REQUEST_COOKIE_NAME);
        return authorizationRequest;
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("authorizationUri", authorizationRequest.getAuthorizationUri());
            data.put("clientId", authorizationRequest.getClientId());
            data.put("redirectUri", authorizationRequest.getRedirectUri());
            data.put("scopes", authorizationRequest.getScopes());
            data.put("state", authorizationRequest.getState());
            data.put("additionalParameters", authorizationRequest.getAdditionalParameters());
            data.put("authorizationRequestUri", authorizationRequest.getAuthorizationRequestUri());
            data.put("attributes", authorizationRequest.getAttributes());

            String json = objectMapper.writeValueAsString(data);
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("OAuth2AuthorizationRequest 직렬화 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private OAuth2AuthorizationRequest deserialize(String value) {
        try {
            String json = new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
            Map<String, Object> data = objectMapper.readValue(json, new TypeReference<>() {});

            return OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri((String) data.get("authorizationUri"))
                    .clientId((String) data.get("clientId"))
                    .redirectUri((String) data.get("redirectUri"))
                    .scopes(new HashSet<>((List<String>) data.get("scopes")))
                    .state((String) data.get("state"))
                    .additionalParameters((Map<String, Object>) data.get("additionalParameters"))
                    .authorizationRequestUri((String) data.get("authorizationRequestUri"))
                    .attributes((Map<String, Object>) data.get("attributes"))
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("OAuth2AuthorizationRequest 역직렬화 실패", e);
        }
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst();
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        if (request.getCookies() == null) return;
        Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .forEach(c -> {
                    Cookie cookie = new Cookie(name, "");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                });
    }
}
