package com.microservices.auth_service.oauth;

import com.microservices.auth_service.dto.LoginResponse;

public record OAuthLoginResult(
    OAuthLoginStatus status,
    LoginResponse loginResponse,
    Long userId,
    String email
) {
    public static OAuthLoginResult loggedIn(LoginResponse loginResponse) {
        return new OAuthLoginResult(OAuthLoginStatus.LOGGED_IN, loginResponse, loginResponse.getUserId(), loginResponse.getEmail());
    }

    public static OAuthLoginResult signupRequired(Long userId, String email) {
        return new OAuthLoginResult(OAuthLoginStatus.SIGNUP_REQUIRED, null, userId, email);
    }
}
