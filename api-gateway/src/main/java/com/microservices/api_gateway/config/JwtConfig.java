package com.microservices.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.common.jwt.JwtTokenProvider;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Date;

@Configuration
public class JwtConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.access-token-expire-time}")
    private long accessTokenExpirationTime;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpirationTime;

    @PostConstruct
    public void checkSystemTime() {
        logger.info("=== 시스템 시간 확인 ===");
        logger.info("현재 시간: {}", new Date());
        logger.info("시스템 시간 밀리초: {}", System.currentTimeMillis());
        logger.info("JVM 시간: {}", Instant.now());
        logger.info("=======================");
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        logger.info("API Gateway - accessTokenExpirationTime: {}", accessTokenExpirationTime);
        logger.info("API Gateway - refreshTokenExpirationTime: {}", refreshTokenExpirationTime);
        logger.info("API Gateway - secretKeyString: '{}'", secretKeyString);
        logger.info("API Gateway - secretKeyString 길이: {}", secretKeyString.length());
        
        return new JwtTokenProvider(secretKeyString, accessTokenExpirationTime, refreshTokenExpirationTime);
    }
}








