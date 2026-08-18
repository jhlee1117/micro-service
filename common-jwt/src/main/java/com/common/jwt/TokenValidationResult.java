package com.common.jwt;

import lombok.Getter;

@Getter
public class TokenValidationResult {
  private final boolean valid;
  private final boolean expired;
  private final String errorMessage;
  private final TokenValidationStatus status;

  private TokenValidationResult(
      boolean valid, boolean expired, String errorMessage, TokenValidationStatus status) {
    this.valid = valid;
    this.expired = expired;
    this.errorMessage = errorMessage;
    this.status = status;
  }

  public static TokenValidationResult valid() {
    return new TokenValidationResult(true, false, null, TokenValidationStatus.VALID);
  }

  public static TokenValidationResult expired(String message) {
    return new TokenValidationResult(false, true, message, TokenValidationStatus.EXPIRED);
  }

  public static TokenValidationResult invalid(String message) {
    return new TokenValidationResult(false, false, message, TokenValidationStatus.INVALID);
  }

  public boolean isValid() {
    return valid;
  }

  public boolean isExpired() {
    return expired;
  }

  public boolean isInvalid() {
    return !valid && !expired;
  }

  public enum TokenValidationStatus {
    VALID,
    EXPIRED,
    INVALID
  }
}
