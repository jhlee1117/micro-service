package com.microservices.auth_service.oauth;

import com.microservices.auth_service.domain.entity.OAuthProvider;

public record OAuthUserProfile(
    OAuthProvider provider,
    String providerUserId,
    String email,
    String name,
    String profileImageUrl
) {
}
