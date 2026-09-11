package com.common.jwt.filter;

import com.common.jwt.authentication.JwtAuthenticationContext;
import com.common.jwt.authentication.JwtAuthenticationHandler;
import com.common.jwt.authentication.JwtUserPrincipal;
import com.common.jwt.authentication.TokenBlacklistService;
import com.common.jwt.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Servlet 환경용 JWT 인증 필터 일반 Spring Boot 애플리케이션에서 사용 */
public class ServletJwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger =
      LoggerFactory.getLogger(ServletJwtAuthenticationFilter.class);

  private final JwtAuthenticationHandler authenticationHandler;

  public ServletJwtAuthenticationFilter(
      JwtAuthenticationHandler authenticationHandler, TokenBlacklistService tokenBlacklistService) {
    this.authenticationHandler = authenticationHandler;
  }

  public ServletJwtAuthenticationFilter(JwtAuthenticationHandler authenticationHandler) {
    this.authenticationHandler = authenticationHandler;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    String method = request.getMethod();

    // Authorization 헤더에서 토큰 추출
    Optional<String> tokenOpt = extractToken(request);

    if (tokenOpt.isEmpty()) {
      // 토큰이 없으면 다음 필터로 진행 (permitAll() 경로에서 처리될 것임)
      logger.debug("JWT 토큰이 없음: path={}, method={}", path, method);
      filterChain.doFilter(request, response);
      return;
    }

    String token = tokenOpt.get();
    String clientIp = getClientIp(request);

    // 토큰 유효성 검사
    JwtAuthenticationContext context =
        authenticationHandler.validateToken(token, path, method, clientIp);

    if (!context.isTokenValid()) {
      authenticationHandler.logAuthenticationFailure(context, "Invalid token");
      authenticationHandler.blacklistInvalidToken(token);
      handleUnauthenticated(response);
      return;
    }

    // 블랙리스트 확인
    if (authenticationHandler.isTokenBlacklisted(token)) {
      authenticationHandler.logAuthenticationFailure(context, "Token is blacklisted");
      handleUnauthenticated(response);
      return;
    }

    // 인증 처리
    Authentication authentication = authenticationHandler.createAuthentication(context);
    if (authentication == null) {
      authenticationHandler.logAuthenticationFailure(context, "Failed to create authentication");
      handleUnauthenticated(response);
      return;
    }

    // SecurityContext에 인증 정보 설정
    SecurityContextHolder.getContext().setAuthentication(authentication);
    if (authentication.getPrincipal() instanceof JwtUserPrincipal principal) {
      TenantContext.setCurrentTenant(principal.getTenantSchema());
    }
    authenticationHandler.logAuthenticationSuccess(context);

    try {
      filterChain.doFilter(request, response);
    } finally {
      // 요청 완료 후 SecurityContext 및 TenantContext 정리
      SecurityContextHolder.clearContext();
      TenantContext.clear();
    }
  }

  private Optional<String> extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    return authenticationHandler.extractToken(authHeader);
  }

  private String getClientIp(HttpServletRequest request) {
    // X-Forwarded-For 헤더 확인
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    // X-Real-IP 헤더 확인
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    // RemoteAddr 사용
    return request.getRemoteAddr();
  }

  private void handleUnauthenticated(HttpServletResponse response) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response
        .getWriter()
        .write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
  }
}
