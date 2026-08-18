package com.common.jwt.authentication;

import com.common.jwt.TokenValidationResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** JWT 인증 컨텍스트 클래스 인증 과정에서 필요한 정보들을 담는 컨텍스트 객체 */
@Getter
@Builder
@AllArgsConstructor
public class JwtAuthenticationContext {

  private final String token;
  private final String path;
  private final String method;
  private final String clientIp;
  private final TokenValidationResult validationResult;
  private final String username;
  private final String tenantId;

  public boolean isTokenValid() {
    return validationResult != null && validationResult.isValid();
  }

  public boolean isTokenExpired() {
    return validationResult != null && validationResult.isExpired();
  }

  public boolean isTokenInvalid() {
    return validationResult != null && validationResult.isInvalid();
  }
}
