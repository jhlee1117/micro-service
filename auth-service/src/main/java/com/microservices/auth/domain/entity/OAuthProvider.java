package com.microservices.auth.domain.entity;

public enum OAuthProvider {
  GOOGLE,
  NAVER,
  GITHUB;

  public static OAuthProvider fromRegistrationId(String registrationId) {
    return OAuthProvider.valueOf(registrationId.toUpperCase());
  }
}
