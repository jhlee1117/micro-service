package com.common.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    public static SecretKey generateSecretKey(String secretKeyString) {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateAccessToken(String username, String tenantId, SecretKey actualSecretKey,
            long accessTokenExpirationTime) {
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

}
