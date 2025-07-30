package com.common.jwt;

import java.util.Date;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@DisplayName("JwtUtil 테스트")
class JwtUtilTest {

    private static final String TEST_SECRET_KEY = "testSecretKeyForJwtTokenGenerationWithMinimum256Bits";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_TENANT_ID = "testTenant";
    private static final long TEST_EXPIRATION_TIME = 3600; // 1시간

    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        secretKey = JwtUtil.generateSecretKey(TEST_SECRET_KEY);
    }

    @Test
    @DisplayName("generateSecretKey - 유효한 시크릿 키 문자열로 SecretKey 생성")
    void generateSecretKey_WithValidString_ShouldCreateSecretKey() {
        // when
        SecretKey result = JwtUtil.generateSecretKey(TEST_SECRET_KEY);

        // then
        assertNotNull(result);
        // 알고리즘은 HmacSHA256 또는 HmacSHA384 또는 HmacSHA512일 수 있음
        String algorithm = result.getAlgorithm();
        assertTrue(algorithm.equals("HmacSHA256") || algorithm.equals("HmacSHA384") || algorithm.equals("HmacSHA512"));
    }

    @Test
    @DisplayName("generateSecretKey - 빈 문자열로 SecretKey 생성 시 예외 발생")
    void generateSecretKey_WithEmptyString_ShouldThrowException() {
        // when & then
        assertThrows(io.jsonwebtoken.security.WeakKeyException.class, () -> {
            JwtUtil.generateSecretKey("");
        });
    }

    @Test
    @DisplayName("generateSecretKey - null 문자열로 SecretKey 생성 시 예외 발생")
    void generateSecretKey_WithNullString_ShouldThrowException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            JwtUtil.generateSecretKey(null);
        });
    }

    @Test
    @DisplayName("generateSecretKey - 짧은 문자열로 SecretKey 생성 시 예외 발생")
    void generateSecretKey_WithShortString_ShouldThrowException() {
        // when & then
        assertThrows(io.jsonwebtoken.security.WeakKeyException.class, () -> {
            JwtUtil.generateSecretKey("short");
        });
    }

    @Test
    @DisplayName("generateAccessToken - 유효한 파라미터로 JWT 토큰 생성")
    void generateAccessToken_WithValidParameters_ShouldCreateValidToken() {
        // when
        String token = JwtUtil.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID, secretKey, TEST_EXPIRATION_TIME);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // 토큰이 올바른 형식인지 확인 (3개의 부분으로 구성)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    @DisplayName("generateAccessToken - 생성된 토큰의 내용 검증")
    void generateAccessToken_ShouldContainCorrectClaims() {
        // when
        String token = JwtUtil.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID, secretKey, TEST_EXPIRATION_TIME);

        // then
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(TEST_USERNAME, claims.getSubject());
        assertEquals(TEST_TENANT_ID, claims.get("tenantId", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("generateAccessToken - 만료 시간이 올바르게 설정되는지 검증")
    void generateAccessToken_ShouldSetCorrectExpirationTime() {
        // when
        long beforeTokenGeneration = System.currentTimeMillis();
        String token = JwtUtil.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID, secretKey, TEST_EXPIRATION_TIME);
        long afterTokenGeneration = System.currentTimeMillis();

        // then
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        // 발급 시간이 현재 시간과 비슷한지 확인
        assertTrue(issuedAt.getTime() >= beforeTokenGeneration - 1000);
        assertTrue(issuedAt.getTime() <= afterTokenGeneration + 1000);

        // 만료 시간이 발급 시간 + 설정된 만료 시간과 비슷한지 확인
        long expectedExpiration = issuedAt.getTime() + (TEST_EXPIRATION_TIME * 1000);
        assertEquals(expectedExpiration, expiration.getTime());
    }

    @Test
    @DisplayName("generateAccessToken - null username으로 토큰 생성")
    void generateAccessToken_WithNullUsername_ShouldCreateToken() {
        // when
        String token = JwtUtil.generateAccessToken(null, TEST_TENANT_ID, secretKey, TEST_EXPIRATION_TIME);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("generateAccessToken - null tenantId로 토큰 생성")
    void generateAccessToken_WithNullTenantId_ShouldCreateToken() {
        // when
        String token = JwtUtil.generateAccessToken(TEST_USERNAME, null, secretKey, TEST_EXPIRATION_TIME);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("generateAccessToken - null SecretKey로 토큰 생성 시 예외 발생")
    void generateAccessToken_WithNullSecretKey_ShouldThrowException() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            JwtUtil.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID, null, TEST_EXPIRATION_TIME);
        });
    }

    @Test
    @DisplayName("generateAccessToken - 음수 만료 시간으로 토큰 생성")
    void generateAccessToken_WithNegativeExpirationTime_ShouldCreateTokenWithPastExpiration() {
        // when
        String token = JwtUtil.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID, secretKey, -3600);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // 만료된 토큰이므로 파싱 시 예외가 발생할 수 있음
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 만료 시간이 과거로 설정되었는지 확인
            assertTrue(claims.getExpiration().before(new Date()));
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 만료된 토큰이므로 예외가 발생하는 것이 정상
            assertTrue(true);
        }
    }

    @Test
    @DisplayName("generateAccessToken - 0 만료 시간으로 토큰 생성")
    void generateAccessToken_WithZeroExpirationTime_ShouldCreateTokenWithCurrentExpiration() {
        // when
        String token = JwtUtil.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID, secretKey, 0);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // 만료된 토큰이므로 파싱 시 예외가 발생할 수 있음
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // 만료 시간이 발급 시간과 같거나 비슷한지 확인
            long timeDifference = Math.abs(claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
            assertTrue(timeDifference < 1000); // 1초 이내
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 만료된 토큰이므로 예외가 발생하는 것이 정상
            assertTrue(true);
        }
    }
} 