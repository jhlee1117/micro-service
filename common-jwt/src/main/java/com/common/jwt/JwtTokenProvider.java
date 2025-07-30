package com.common.jwt;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

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

    // ��ū�� ��ȿ���� �˻��ϴ� �޼���
    public boolean validateToken(String token) { 
        try {
            log.debug("Validating token with secret key algorithm: {}", actualSecretKey.getAlgorithm());
            log.debug("Token to validate: {}", token);
            
            Jwts.parser()
                .verifyWith(actualSecretKey)
                .build()
                .parseSignedClaims(token);
                
            return true; // ��ȿ�� ��ū
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
            log.error("Token validation failed with exception: ", e);
            return false;
        }
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

}
