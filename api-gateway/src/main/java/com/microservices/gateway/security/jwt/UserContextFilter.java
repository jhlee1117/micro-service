package com.microservices.gateway.security.jwt;

import com.common.jwt.authentication.JwtUserPrincipal;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserContextFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(Authentication::isAuthenticated)
        .map(
            authentication -> {
              // SecurityContext에서 userId를 추출하여 헤더에 추가
              String userId = authentication.getName();
              ServerHttpRequest.Builder requestBuilder =
                  exchange.getRequest().mutate().header("X-User-Id", userId);

              if (authentication.getPrincipal() instanceof JwtUserPrincipal principal) {
                if (principal.getTenantSchema() != null) {
                  requestBuilder.header("X-Tenant-Schema", principal.getTenantSchema());
                }
                String roles =
                    authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");
                requestBuilder.header("X-User-Roles", roles);
              }

              ServerHttpRequest mutatedRequest = requestBuilder.build();
              return exchange.mutate().request(mutatedRequest).build();
            })
        .defaultIfEmpty(exchange)
        .flatMap(chain::filter);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
