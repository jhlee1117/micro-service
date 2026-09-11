package com.common.jwt.tenant;

/** 현재 요청을 처리 중인 스레드의 테넌트 스키마 식별자를 보관하는 ThreadLocal 홀더. */
public final class TenantContext {

  private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

  private TenantContext() {}

  public static void setCurrentTenant(String tenantSchema) {
    CURRENT_TENANT.set(tenantSchema);
  }

  public static String getCurrentTenant() {
    return CURRENT_TENANT.get();
  }

  public static void clear() {
    CURRENT_TENANT.remove();
  }
}
