package com.ddiring.ddiring_server.global.security.application.service;

import com.ddiring.ddiring_server.domain.user.domain.entity.User;
import com.ddiring.ddiring_server.domain.user.domain.repository.UserRepository;
import com.ddiring.ddiring_server.global.security.application.dto.OAuthAttributes;
import com.ddiring.ddiring_server.global.security.vo.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
                oAuth2User.getAttributes());

        User user = saveOrFind(attributes);
        boolean isNewUser = user.getRole() == null;

        return new CustomUser(
                user.getId(),
                isNewUser,
                AuthorityUtils.NO_AUTHORITIES,
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrFind(OAuthAttributes attributes) {
        return userRepository.findByKakaoId(attributes.getId())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .kakaoId(attributes.getId())
                                .profileImageUrl(attributes.getPicture())
                                .authProvider("kakao")
                                .build()
                ));
    }
}
