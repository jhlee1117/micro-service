package com.microservices.board.tenant;

import com.common.jwt.tenant.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

  public static final String DEFAULT_TENANT = "public";

  @Override
  public String resolveCurrentTenantIdentifier() {
    String currentTenant = TenantContext.getCurrentTenant();
    return currentTenant != null ? currentTenant : DEFAULT_TENANT;
  }

  @Override
  public boolean validateExistingCurrentSessions() {
    return true;
  }
}
