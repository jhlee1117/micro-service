package com.microservices.auth.config;

import com.common.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

  @Value("${jwt.secret:defaultSecretKey}")
  private String jwtSecret;

  @Value("${jwt.expiration:3600000}") // 기본값: 1시간 (밀리초)
  private long jwtExpiration;

  @Value("${jwt.refresh-expiration:2592000000}") // 기본값: 1일 (밀리초)
  private long jwtRefreshExpiration;

  @Bean
  public JwtTokenProvider jwtTokenProvider() {
    log.info("Auth Service - jwtSecret: '{}'", jwtSecret);
    log.info("Auth Service - jwtSecret 길이: {}", jwtSecret.length());

    return new JwtTokenProvider(jwtSecret, jwtExpiration, jwtRefreshExpiration);
  }
}
