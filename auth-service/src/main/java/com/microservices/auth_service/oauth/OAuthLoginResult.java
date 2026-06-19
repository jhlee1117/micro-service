package com.microservices.auth_service.oauth;

import com.microservices.auth_service.dto.LoginResponse;

public record OAuthLoginResult(
    OAuthLoginStatus status,
    LoginResponse loginResponse,
    Long userId,
    String email,
    OAuthPendingSignup pendingSignup
) {
    public static OAuthLoginResult loggedIn(LoginResponse loginResponse) {
        return new OAuthLoginResult(OAuthLoginStatus.LOGGED_IN, loginResponse, loginResponse.getUserId(), loginResponse.getEmail(), null);
    }

    public static OAuthLoginResult pending(
        Long userId,
        String email,
        OAuthPendingSignup pendingSignup) {
        return new OAuthLoginResult(OAuthLoginStatus.PENDING, null, userId, email, pendingSignup);
    }
}
