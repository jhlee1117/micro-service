package com.microservices.api_gateway.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String secretKeyString; // JWT 비밀 키

    @Value("${jwt.access-token-expire-time}") // application.yml에 설정된 만료 시간
    private long accessTokenExpirationTime; // 액세스 토큰 만료 시간 (단위: 초)

    private SecretKey actualSecretKey; // SecretKey 객체

    @PostConstruct // 초기화 메서드
    public void init() {
        this.actualSecretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, String tenantId) {
        Date now = new Date();
        // accessTokenExpirationTime에 설정된 만료 시간을 사용하여 만료 날짜를 계산합니다.
        Date expiryDate = new Date(now.getTime() + (accessTokenExpirationTime * 1000)); 

        return Jwts.builder()
                .subject(username)
                .issuedAt(now) // 토큰 발급 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .claim("tenantId", tenantId)
                .signWith(actualSecretKey) // SecretKey를 사용하여 서명
                .compact();
    }

    // 토큰의 유효성을 검사하는 메서드
    public boolean validateToken(String token) { 
        try {
            Jwts.parser()
                .verifyWith(actualSecretKey) // SecretKey를 사용하여 검증
                .build()
                .parseSignedClaims(token);
            return true; // 유효한 토큰
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", token, e);
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(actualSecretKey) // SecretKey를 사용하여 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String username = claims.getSubject();
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }
    // public String getUsernameFromToken(String token) { ... }
    // public Claims getAllClaimsFromToken(String token) { ... }

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
