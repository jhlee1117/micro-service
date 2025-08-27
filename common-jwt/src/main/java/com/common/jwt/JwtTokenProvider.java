package com.common.jwt;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.time.Instant;
import java.util.Date;

public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    // JWT token secret key
    private final String secretKeyString;
    private final long accessTokenExpirationTime;
    private SecretKey actualSecretKey;
    private final long refreshTokenExpirationTime;

    public JwtTokenProvider(String secretKeyString, long accessTokenExpirationTime, long refreshTokenExpirationTime) {
        this.secretKeyString = secretKeyString;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
        initializeSecretKey();
    }

    private void initializeSecretKey() {
        // Initialize the actual secret key using the provided secret key string
        this.actualSecretKey = JwtUtil.generateSecretKey(secretKeyString);
    }

    public String generateAccessToken(String username, String tenantId) {
        // Generate an access token using the actual secret key
        return JwtUtil.generateAccessToken(username, tenantId, actualSecretKey, accessTokenExpirationTime);
    }

    public String generateRefreshToken(String username, String tenantId) {
        return JwtUtil.generateRefreshToken(username, tenantId, actualSecretKey, refreshTokenExpirationTime);
    }

    public boolean validateToken(String token) { 
        return validateTokenWithResult(token).isValid();
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims.get("type").equals("refresh")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }

    public Claims getClaims(String token) {
        // ��ū���� Claims�� �����ϴ� �޼���
        return Jwts.parser()
                .verifyWith(actualSecretKey) // SecretKey�� ����Ͽ� ����
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        // ��ū���� ����� �̸��� �����ϴ� �޼���
        return getClaims(token).getSubject();
    }

    public String getTenantId(String token) {
        // ��ū���� tenantId�� �����ϴ� �޼���
        return getClaims(token).get("tenantId", String.class);
    }

    public SecretKey getActualSecretKey() {
        return actualSecretKey;
    }

    public String getSecretKeyString() {
        return secretKeyString;
    }

    public String getUsernameFromRefreshToken(String token) {
        return getClaims(token).getSubject();
    }
    
    public String getTenantIdFromRefreshToken(String token) {
        return getClaims(token).get("tenantId", String.class);
    }

    public long getRefreshTokenExpirationTime() {
        return refreshTokenExpirationTime;
    }

    public TokenValidationResult validateTokenWithResult(String token) {
        try {
            log.info("=== 토큰 검증 시작 ===");
            log.info("검증 시점: {}", new Date());
            log.info("검증할 토큰: {}", token);
            
            // 토큰 내용 미리 확인
            try {
                Claims claims = getClaims(token);
                Date expiration = claims.getExpiration();
                Date now = new Date();
                log.info("토큰 만료 시간: {}", expiration);
                log.info("현재 시간: {}", now);
                log.info("만료 여부: {}", now.after(expiration));
            } catch (Exception e) {
                log.warn("토큰 내용 미리 확인 실패: {}", e.getMessage());
            }
            
            Jwts.parser()
                .verifyWith(actualSecretKey)
                .build()
                .parseSignedClaims(token);
                
            log.info("토큰 검증 성공 - 유효함");
            return TokenValidationResult.valid(); 
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료됨: {}", e.getMessage());
            return TokenValidationResult.expired(e.getMessage());
        } catch (Exception e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return TokenValidationResult.invalid("Invalid token");
        }
    }

    public void debugToken(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();
            Date now = new Date();
            
            log.info("=== JWT 토큰 디버그 정보 ===");
            log.info("현재 시간: {}", now);
            log.info("토큰 발급 시간: {}", issuedAt);
            log.info("토큰 만료 시간: {}", expiration);
            log.info("현재 시간과 만료 시간 차이: {} ms", expiration.getTime() - now.getTime());
            log.info("토큰이 만료되었는가: {}", now.after(expiration));
            log.info("===============================");
        } catch (Exception e) {
            log.error("토큰 디버그 실패: {}", e.getMessage());
        }
    }
}
