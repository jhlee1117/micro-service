package com.common.jwt.authentication;

import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

/** JWT에서 추출한 사용자 정보를 담는 Authentication principal. */
@Getter
public class JwtUserPrincipal implements AuthenticatedPrincipal {

  private final String username;
  private final String tenantId;
  private final String tenantSchema;

  public JwtUserPrincipal(String username, String tenantId, String tenantSchema) {
    this.username = username;
    this.tenantId = tenantId;
    this.tenantSchema = tenantSchema;
  }

  @Override
  public String getName() {
    return username;
  }
}
