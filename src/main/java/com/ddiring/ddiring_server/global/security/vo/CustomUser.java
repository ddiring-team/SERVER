package com.ddiring.ddiring_server.global.security.vo;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomUser extends DefaultOAuth2User {

    private final Long userId;
    private final boolean isNewUser;

    public CustomUser(Long userId, boolean isNewUser,
                      Collection<? extends GrantedAuthority> authorities,
                      Map<String, Object> attributes, String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
        this.userId = userId;
        this.isNewUser = isNewUser;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isNewUser() {
        return isNewUser;
    }
}
