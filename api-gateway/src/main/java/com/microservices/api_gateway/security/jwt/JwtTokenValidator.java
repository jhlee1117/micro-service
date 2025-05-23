package com.microservices.api_gateway.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.common.jwt.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenValidator {

    // private final JwtTokenValidator jwtTokenValidator;

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);

    @Value("${jwt.secret}")
    private String secretKeyString; // JWT 비밀 키

    @Value("${jwt.access-token-expire-time}") // application.yml에 설정된 만료 시간
    private long accessTokenExpirationTime; // 액세스 토큰 만료 시간 (단위: 초)

    JwtTokenProvider jwtTokenProvider;

    @PostConstruct
    public void init() {
        jwtTokenProvider = new JwtTokenProvider(secretKeyString, accessTokenExpirationTime);
    }    

    public Authentication getAuthentication(String token) {
        Claims claims = jwtTokenProvider.getClaims(token);
        String username = claims.getSubject();
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }

    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            logger.error("JWT 토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {

        Date now = new Date();
        // accessTokenExpirationTime에 설정된 만료 시간을 사용하여 만료 날짜를 계산합니다.
        Date expiryDate = new Date(now.getTime() + (3600 * 1000)); 

        String token =  Jwts.builder()
                .subject("testuser")
                .issuedAt(now) // 토큰 발급 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .claim("tenantId", "tenant1")
                .signWith(Keys.hmacShaKeyFor("lFMCnn04hFUty4RnSNjDDfQ8eYBySWTWis5AmbGiv6U=".getBytes(StandardCharsets.UTF_8)))
                .compact();
        System.out.println("Generated Token: " + token);
    }
}
