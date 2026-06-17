package com.microservices.auth_service.oauth;

import java.util.Map;

import com.microservices.auth_service.domain.entity.OAuthProvider;

public interface OAuthUserProfileExtractor {

    OAuthProvider provider();

    OAuthUserProfile extract(Map<String, Object> attributes);
}
