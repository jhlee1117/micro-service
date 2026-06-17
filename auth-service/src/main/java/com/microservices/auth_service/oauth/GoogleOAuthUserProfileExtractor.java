package com.microservices.auth_service.oauth;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.microservices.auth_service.domain.entity.OAuthProvider;

@Component
public class GoogleOAuthUserProfileExtractor implements OAuthUserProfileExtractor {

    @Override
    public OAuthProvider provider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public OAuthUserProfile extract(Map<String, Object> attributes) {
        return new OAuthUserProfile(
            provider(),
            stringValue(attributes.get("sub")),
            stringValue(attributes.get("email")),
            stringValue(attributes.get("name")),
            stringValue(attributes.get("picture"))
        );
    }

    private String stringValue(Object value) {
        return value != null ? value.toString() : null;
    }
}
