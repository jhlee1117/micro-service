package com.common.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;

public class JwtUtil {

  public static SecretKey generateSecretKey(String secretKeyString) {
    try {
      // Base64 디코딩 시도
      byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (IllegalArgumentException e) {
      // Base64 디코딩 실패 시 원본 문자열 사용
      return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }
  }

  public static String generateAccessToken(
      String username,
      String tenantId,
      String tenantSchema,
      List<String> roles,
      SecretKey actualSecretKey,
      long accessTokenExpirationTime) {
    Date now = new Date();
    // accessTokenExpirationTime에 설정된 만료 시간을 사용하여 만료 날짜를 계산합니다.
    Date expiryDate = new Date(now.getTime() + accessTokenExpirationTime);

    return Jwts.builder()
        .subject(username)
        .issuedAt(now) // 토큰 발급 시간
        .expiration(expiryDate) // 토큰 만료 시간
        .claim("tenantId", tenantId)
        .claim("tenantSchema", tenantSchema)
        .claim("roles", roles)
        .signWith(actualSecretKey) // SecretKey를 사용하여 서명
        .compact();
  }

  public static String generateRefreshToken(
      String username,
      String tenantId,
      SecretKey actualSecretKey,
      long refreshTokenExpirationTime) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + refreshTokenExpirationTime);

    return Jwts.builder()
        .subject(username)
        .issuedAt(now)
        .expiration(expiryDate)
        .claim("tenantId", tenantId)
        .claim("type", "refresh") // refresh token임을 명시
        .claim("version", "1.0") // 토큰 버전 관리 (선택사항)
        .signWith(actualSecretKey)
        .compact();
  }
}
