package com.common.jwt;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtTokenProvider {

    // JWT token secret key
    private final String secretKeyString;
    private final long accessTokenExpirationTime;
    private SecretKey actualSecretKey;

    public JwtTokenProvider(String secretKeyString, long accessTokenExpirationTime) {
        this.secretKeyString = secretKeyString;
        this.accessTokenExpirationTime = accessTokenExpirationTime;
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

    // 토큰의 유효성을 검사하는 메서드
    public boolean validateToken(String token) { 
        try {
            Jwts.parser()
                .verifyWith(actualSecretKey) // SecretKey를 사용하여 검증
                .build()
                .parseSignedClaims(token);
            return true; // 유효한 토큰
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        // 토큰에서 Claims를 추출하는 메서드
        return Jwts.parser()
                .verifyWith(actualSecretKey) // SecretKey를 사용하여 검증
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        // 토큰에서 사용자 이름을 추출하는 메서드
        return getClaims(token).getSubject();
    }

    public String getTenantId(String token) {
        // 토큰에서 tenantId를 추출하는 메서드
        return getClaims(token).get("tenantId", String.class);
    }

    public SecretKey getActualSecretKey() {
        return actualSecretKey;
    }

}
