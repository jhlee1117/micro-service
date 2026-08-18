package com.common.jwt.filter;

import com.common.jwt.authentication.JwtAuthenticationContext;
import com.common.jwt.authentication.JwtAuthenticationHandler;
import com.common.jwt.authentication.TokenBlacklistService;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/** Reactive 환경용 JWT 인증 필터 Spring Cloud Gateway와 같은 WebFlux 기반 애플리케이션에서 사용 */
public class ReactiveJwtAuthenticationFilter implements WebFilter {

  private static final Logger logger =
      LoggerFactory.getLogger(ReactiveJwtAuthenticationFilter.class);

  private final JwtAuthenticationHandler authenticationHandler;
  private final TokenBlacklistService tokenBlacklistService;

  public ReactiveJwtAuthenticationFilter(
      JwtAuthenticationHandler authenticationHandler, TokenBlacklistService tokenBlacklistService) {
    this.authenticationHandler = authenticationHandler;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  public ReactiveJwtAuthenticationFilter(JwtAuthenticationHandler authenticationHandler) {
    this.authenticationHandler = authenticationHandler;
    this.tokenBlacklistService = null;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    String path = request.getPath().toString();
    String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";

    // Authorization 헤더에서 토큰 추출
    Optional<String> tokenOpt = extractToken(request);

    if (tokenOpt.isEmpty()) {
      // 토큰이 없으면 다음 필터로 진행 (permitAll() 경로에서 처리될 것임)
      logger.debug("JWT 토큰이 없음: path={}, method={}", path, method);
      return chain.filter(exchange);
    }

    String token = tokenOpt.get();
    String clientIp = getClientIp(request);

    // 토큰 유효성 검사
    JwtAuthenticationContext context =
        authenticationHandler.validateToken(token, path, method, clientIp);

    if (!context.isTokenValid()) {
      authenticationHandler.logAuthenticationFailure(context, "Invalid token");
      authenticationHandler.blacklistInvalidToken(token);
      return handleUnauthenticated(exchange);
    }

    // 블랙리스트 확인 (비동기)
    return checkBlacklistAndProceed(exchange, chain, context);
  }

  private Optional<String> extractToken(ServerHttpRequest request) {
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    return authenticationHandler.extractToken(authHeader);
  }

  private String getClientIp(ServerHttpRequest request) {
    // X-Forwarded-For 헤더 확인
    String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    // X-Real-IP 헤더 확인
    String xRealIp = request.getHeaders().getFirst("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    // RemoteAddress 사용
    if (request.getRemoteAddress() != null) {
      return request.getRemoteAddress().getAddress().getHostAddress();
    }

    return "unknown";
  }

  private Mono<Void> checkBlacklistAndProceed(
      ServerWebExchange exchange, WebFilterChain chain, JwtAuthenticationContext context) {
    if (tokenBlacklistService == null) {
      return proceedWithAuthentication(exchange, chain, context);
    }

    return tokenBlacklistService
        .isBlacklistedAsync(context.getToken())
        .flatMap(
            isBlacklisted -> {
              if (Boolean.TRUE.equals(isBlacklisted)) {
                authenticationHandler.logAuthenticationFailure(context, "Token is blacklisted");
                return handleUnauthenticated(exchange);
              } else {
                return proceedWithAuthentication(exchange, chain, context);
              }
            })
        .onErrorResume(
            error -> {
              logger.error("블랙리스트 확인 중 오류 발생: {}", error.getMessage());
              // 오류 시 허용적으로 처리하여 서비스 중단을 방지
              return proceedWithAuthentication(exchange, chain, context);
            });
  }

  private Mono<Void> proceedWithAuthentication(
      ServerWebExchange exchange, WebFilterChain chain, JwtAuthenticationContext context) {
    Authentication authentication = authenticationHandler.createAuthentication(context);
    if (authentication == null) {
      authenticationHandler.logAuthenticationFailure(context, "Failed to create authentication");
      return handleUnauthenticated(exchange);
    }

    authenticationHandler.logAuthenticationSuccess(context);

    return chain
        .filter(exchange)
        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
  }

  private Mono<Void> handleUnauthenticated(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }
}
