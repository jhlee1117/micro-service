package com.microservices.auth.oauth;

import com.microservices.auth.domain.entity.OAuthProvider;

public record OAuthUserProfile(
    OAuthProvider provider,
    String providerUserId,
    String email,
    String name,
    String profileImageUrl) {}
