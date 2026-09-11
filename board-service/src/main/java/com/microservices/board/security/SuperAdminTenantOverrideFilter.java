package com.microservices.board.security;

import com.common.jwt.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * ROLE_SUPER_ADMIN 사용자가 {@code X-Target-Tenant-Schema} 헤더로 임의 테넌트의 데이터를 조회/전환할 수 있게 한다. 그 외
 * 사용자가 이 헤더를 보내도 무시된다 - 클라이언트가 보낸 값을 그 자체로 신뢰하지 않고, 인증된 권한을 서버가 직접 확인한다.
 */
public class SuperAdminTenantOverrideFilter extends OncePerRequestFilter {

  private static final String TARGET_TENANT_HEADER = "X-Target-Tenant-Schema";
  private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";
  private static final Pattern SAFE_SCHEMA_NAME = Pattern.compile("^[a-zA-Z0-9_]+$");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String targetTenant = request.getHeader(TARGET_TENANT_HEADER);
    if (targetTenant != null
        && isSuperAdmin()
        && SAFE_SCHEMA_NAME.matcher(targetTenant).matches()) {
      TenantContext.setCurrentTenant(targetTenant);
    }

    filterChain.doFilter(request, response);
  }

  private boolean isSuperAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return false;
    }

    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(SUPER_ADMIN_ROLE::equals);
  }
}
