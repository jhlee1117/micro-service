package com.microservices.auth.oauth;

public record OAuthPendingSignup(String signupToken, String username, String name, Long tenantId) {}
