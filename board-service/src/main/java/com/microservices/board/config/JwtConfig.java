package com.microservices.board.config;

import com.common.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration:3600000}")
  private long jwtExpiration;

  @Value("${jwt.refresh-expiration:2592000000}")
  private long jwtRefreshExpiration;

  @Bean
  public JwtTokenProvider jwtTokenProvider() {
    log.info("Board Service - JWT secret configured. length: {}", jwtSecret.length());

    return new JwtTokenProvider(jwtSecret, jwtExpiration, jwtRefreshExpiration);
  }
}
