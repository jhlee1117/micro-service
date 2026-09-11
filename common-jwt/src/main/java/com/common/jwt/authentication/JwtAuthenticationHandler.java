package com.common.jwt.authentication;

import com.common.jwt.JwtTokenProvider;
import com.common.jwt.TokenValidationResult;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** JWT 인증 처리 핸들러 공통 JWT 인증 로직을 담당하는 핸들러 클래스 */
public class JwtAuthenticationHandler {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationHandler.class);

  private final JwtTokenProvider jwtTokenProvider;
  private final TokenBlacklistService tokenBlacklistService;

  public JwtAuthenticationHandler(
      JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  public JwtAuthenticationHandler(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.tokenBlacklistService = null;
  }

  /** JWT 토큰에서 토큰 문자열 추출 */
  public Optional<String> extractToken(String authorizationHeader) {
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      return Optional.of(authorizationHeader.substring(7));
    }
    return Optional.empty();
  }

  /** JWT 토큰 검증 및 인증 컨텍스트 생성 */
  public JwtAuthenticationContext validateToken(
      String token, String path, String method, String clientIp) {
    logger.debug("JWT 토큰 검증 시작: path={}, method={}", path, method);

    TokenValidationResult validationResult = jwtTokenProvider.validateTokenWithResult(token);
    logger.info(
        "토큰 검증 결과: valid={}, status={}", validationResult.isValid(), validationResult.getStatus());

    JwtAuthenticationContext.JwtAuthenticationContextBuilder contextBuilder =
        JwtAuthenticationContext.builder()
            .token(token)
            .path(path)
            .method(method)
            .clientIp(clientIp)
            .validationResult(validationResult);

    if (validationResult.isValid()) {
      try {
        String username = jwtTokenProvider.getUsername(token);
        String tenantId = jwtTokenProvider.getTenantId(token);
        String tenantSchema = jwtTokenProvider.getTenantSchema(token);
        List<String> roles = jwtTokenProvider.getRoles(token);
        contextBuilder
            .username(username)
            .tenantId(tenantId)
            .tenantSchema(tenantSchema)
            .roles(roles);
        logger.debug(
            "토큰에서 사용자 정보 추출 성공: username={}, tenantId={}, tenantSchema={}",
            username,
            tenantId,
            tenantSchema);
      } catch (Exception e) {
        logger.error("토큰에서 사용자 정보 추출 실패: {}", e.getMessage());
        return contextBuilder
            .validationResult(
                TokenValidationResult.invalid("Failed to extract user info from token"))
            .build();
      }
    }

    return contextBuilder.build();
  }

  /** 블랙리스트 확인 (동기식) */
  public boolean isTokenBlacklisted(String token) {
    if (tokenBlacklistService == null) {
      return false;
    }

    try {
      boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
      logger.debug("토큰 블랙리스트 확인 결과: {}", isBlacklisted);
      return isBlacklisted;
    } catch (Exception e) {
      logger.error("블랙리스트 확인 중 오류 발생: {}", e.getMessage());
      return false; // 오류 시 허용적으로 처리
    }
  }

  /** 유효하지 않은 토큰을 블랙리스트에 추가 */
  public void blacklistInvalidToken(String token) {
    if (tokenBlacklistService == null) {
      return;
    }

    try {
      // 24시간 동안 블랙리스트에 유지
      tokenBlacklistService.blacklistToken(token, 1000L * 60 * 60 * 24);
      logger.info("유효하지 않은 토큰을 블랙리스트에 추가했습니다");
    } catch (Exception e) {
      logger.error("토큰 블랙리스트 추가 중 오류 발생: {}", e.getMessage());
    }
  }

  /** JWT 인증 컨텍스트에서 Spring Security Authentication 객체 생성 */
  public Authentication createAuthentication(JwtAuthenticationContext context) {
    if (!context.isTokenValid() || context.getUsername() == null) {
      return null;
    }

    List<String> roleNames = context.getRoles();
    List<SimpleGrantedAuthority> authorities =
        (roleNames == null || roleNames.isEmpty())
            ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
            : roleNames.stream().map(SimpleGrantedAuthority::new).toList();

    JwtUserPrincipal principal =
        new JwtUserPrincipal(
            context.getUsername(), context.getTenantId(), context.getTenantSchema());

    return new UsernamePasswordAuthenticationToken(principal, null, authorities);
  }

  /** 인증 실패 로그 기록 */
  public void logAuthenticationFailure(JwtAuthenticationContext context, String reason) {
    logger.warn(
        "JWT 인증 실패: path={}, method={}, reason={}, clientIp={}",
        context.getPath(),
        context.getMethod(),
        reason,
        context.getClientIp());
  }

  /** 인증 성공 로그 기록 */
  public void logAuthenticationSuccess(JwtAuthenticationContext context) {
    logger.info(
        "JWT 인증 성공: path={}, method={}, username={}, clientIp={}",
        context.getPath(),
        context.getMethod(),
        context.getUsername(),
        context.getClientIp());
  }
}
