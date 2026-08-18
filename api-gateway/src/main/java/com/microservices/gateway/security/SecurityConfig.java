package com.microservices.gateway.security;

import com.common.jwt.JwtTokenProvider;
import com.common.jwt.config.JwtFilterConfigurer;
import com.common.jwt.filter.ReactiveJwtAuthenticationFilter;
import com.microservices.gateway.security.redis.RedisTokenBlacklistAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTokenBlacklistAdapter tokenBlacklistService;

  public SecurityConfig(
      JwtTokenProvider jwtTokenProvider, RedisTokenBlacklistAdapter tokenBlacklistService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

    // 공통 JWT 필터 생성
    ReactiveJwtAuthenticationFilter jwtAuthenticationFilter =
        JwtFilterConfigurer.createReactiveFilter(jwtTokenProvider, tokenBlacklistService);

    return http.csrf(csrfCustomizer -> csrfCustomizer.disable())
        .cors(corsCustomizer -> corsCustomizer.disable()) // CORS 비활성화 (필요시 별도 설정)
        .exceptionHandling(
            exceptionHandlingCustomizer ->
                exceptionHandlingCustomizer.authenticationEntryPoint(
                    (exchange, ex) -> {
                      exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                      return exchange.getResponse().setComplete();
                    }))
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers(
                        "/auth/login",
                        "/auth/register",
                        "/auth/hello",
                        "/auth/refresh",
                        "/auth/oauth/signup/complete",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/public/**",
                        "/tenant/list")
                    .permitAll()
                    .anyExchange()
                    .authenticated())
        .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
        .build();
  }
}
