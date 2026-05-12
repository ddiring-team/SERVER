package com.ddiring.ddiring_server.global.security.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {

    private String id;
    private String picture;
    private Map<String, Object> attributes;
    private String nameAttributeKey;

    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {
        if ("kakao".equals(registrationId)) {
            return ofKakao(userNameAttributeName, attributes);
        }
        throw new IllegalArgumentException("지원하지 않는 OAuth2 제공자입니다: " + registrationId);
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Long id = (Long) attributes.get("id");

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String profileImageUrl = (String) profile.get("profile_image_url");

        return OAuthAttributes.builder()
                .id(String.valueOf(id))
                .picture(profileImageUrl)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }
}
