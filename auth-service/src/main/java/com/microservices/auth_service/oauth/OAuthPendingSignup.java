package com.microservices.auth_service.oauth;

public record OAuthPendingSignup(
    String signupToken,
    String username,
    String name,
    Long tenantId
) {
}
