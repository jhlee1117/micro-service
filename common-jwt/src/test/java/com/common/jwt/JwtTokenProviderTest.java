package com.common.jwt;

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

@DisplayName("JwtTokenProvider 테스트")
class JwtTokenProviderTest {

    private static final String TEST_SECRET_KEY = "testSecretKeyForJwtTokenGenerationWithMinimum256Bits";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_TENANT_ID = "testTenant";
    private static final long TEST_EXPIRATION_TIME = 3600; // 1시간

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET_KEY, TEST_EXPIRATION_TIME);
    }

    @Test
    @DisplayName("생성자 - 유효한 파라미터로 JwtTokenProvider 생성")
    void constructor_WithValidParameters_ShouldCreateInstance() {
        // when
        JwtTokenProvider provider = new JwtTokenProvider(TEST_SECRET_KEY, TEST_EXPIRATION_TIME);

        // then
        assertNotNull(provider);
        assertEquals(TEST_SECRET_KEY, provider.getSecretKeyString());
        assertNotNull(provider.getActualSecretKey());
    }

    @Test
    @DisplayName("생성자 - null 시크릿 키로 생성 시 예외 발생")
    void constructor_WithNullSecretKey_ShouldThrowException() {
        // when & then
        assertThrows(NullPointerException.class, () -> {
            new JwtTokenProvider(null, TEST_EXPIRATION_TIME);
        });
    }

    @Test
    @DisplayName("생성자 - 빈 시크릿 키로 생성 시 예외 발생")
    void constructor_WithEmptySecretKey_ShouldThrowException() {
        // when & then
        assertThrows(io.jsonwebtoken.security.WeakKeyException.class, () -> {
            new JwtTokenProvider("", TEST_EXPIRATION_TIME);
        });
    }

    @Test
    @DisplayName("generateAccessToken - 유효한 파라미터로 JWT 토큰 생성")
    void generateAccessToken_WithValidParameters_ShouldCreateValidToken() {
        // when
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

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
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

        // then
        Claims claims = Jwts.parser()
                .verifyWith(jwtTokenProvider.getActualSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(TEST_USERNAME, claims.getSubject());
        assertEquals(TEST_TENANT_ID, claims.get("tenantId", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("generateAccessToken - null username으로 토큰 생성")
    void generateAccessToken_WithNullUsername_ShouldCreateToken() {
        // when
        String token = jwtTokenProvider.generateAccessToken(null, TEST_TENANT_ID);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("generateAccessToken - null tenantId로 토큰 생성")
    void generateAccessToken_WithNullTenantId_ShouldCreateToken() {
        // when
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME, null);

        // then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("validateToken - 유효한 토큰 검증")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("validateToken - 잘못된 시크릿 키로 서명된 토큰 검증")
    void validateToken_WithTokenSignedByDifferentKey_ShouldReturnFalse() {
        // given
        JwtTokenProvider differentProvider = new JwtTokenProvider("differentSecretKeyForJwtTokenGenerationWithMinimum256Bits", TEST_EXPIRATION_TIME);
        String token = differentProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - 만료된 토큰 검증")
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // given
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(TEST_SECRET_KEY, -3600); // 과거 만료 시간
        String expiredToken = shortExpirationProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

        // when
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - 잘못된 형식의 토큰 검증")
    void validateToken_WithInvalidTokenFormat_ShouldReturnFalse() {
        // given
        String invalidToken = "invalid.token.format";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - null 토큰 검증")
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // when
        boolean isValid = jwtTokenProvider.validateToken(null);

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("validateToken - 빈 토큰 검증")
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        // when
        boolean isValid = jwtTokenProvider.validateToken("");

        // then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("getClaims - 유효한 토큰에서 Claims 추출")
    void getClaims_WithValidToken_ShouldReturnClaims() {
        // given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

        // when
        Claims claims = jwtTokenProvider.getClaims(token);

        // then
        assertNotNull(claims);
        assertEquals(TEST_USERNAME, claims.getSubject());
        assertEquals(TEST_TENANT_ID, claims.get("tenantId", String.class));
    }

    @Test
    @DisplayName("getClaims - 잘못된 토큰에서 Claims 추출 시 예외 발생")
    void getClaims_WithInvalidToken_ShouldThrowException() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getClaims(invalidToken);
        });
    }

    @Test
    @DisplayName("getUsername - 유효한 토큰에서 사용자명 추출")
    void getUsername_WithValidToken_ShouldReturnUsername() {
        // given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

        // when
        String username = jwtTokenProvider.getUsername(token);

        // then
        assertEquals(TEST_USERNAME, username);
    }

    @Test
    @DisplayName("getUsername - 잘못된 토큰에서 사용자명 추출 시 예외 발생")
    void getUsername_WithInvalidToken_ShouldThrowException() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsername(invalidToken);
        });
    }

    @Test
    @DisplayName("getTenantId - 유효한 토큰에서 tenantId 추출")
    void getTenantId_WithValidToken_ShouldReturnTenantId() {
        // given
        String token = jwtTokenProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

        // when
        String tenantId = jwtTokenProvider.getTenantId(token);

        // then
        assertEquals(TEST_TENANT_ID, tenantId);
    }

    @Test
    @DisplayName("getTenantId - 잘못된 토큰에서 tenantId 추출 시 예외 발생")
    void getTenantId_WithInvalidToken_ShouldThrowException() {
        // given
        String invalidToken = "invalid.token.format";

        // when & then
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getTenantId(invalidToken);
        });
    }

    @Test
    @DisplayName("getActualSecretKey - SecretKey 반환")
    void getActualSecretKey_ShouldReturnSecretKey() {
        // when
        SecretKey secretKey = jwtTokenProvider.getActualSecretKey();

        // then
        assertNotNull(secretKey);
        // 알고리즘은 HmacSHA256 또는 HmacSHA384 또는 HmacSHA512일 수 있음
        String algorithm = secretKey.getAlgorithm();
        assertTrue(algorithm.equals("HmacSHA256") || algorithm.equals("HmacSHA384") || algorithm.equals("HmacSHA512"));
    }

    @Test
    @DisplayName("getSecretKeyString - 시크릿 키 문자열 반환")
    void getSecretKeyString_ShouldReturnSecretKeyString() {
        // when
        String secretKeyString = jwtTokenProvider.getSecretKeyString();

        // then
        assertEquals(TEST_SECRET_KEY, secretKeyString);
    }

    @Test
    @DisplayName("토큰 생성 후 검증 및 정보 추출 통합 테스트")
    void integrationTest_TokenGenerationValidationAndExtraction() {
        // given
        String username = "integrationUser";
        String tenantId = "integrationTenant";

        // when - 토큰 생성
        String token = jwtTokenProvider.generateAccessToken(username, tenantId);

        // then - 토큰 검증
        assertTrue(jwtTokenProvider.validateToken(token));

        // then - 정보 추출
        assertEquals(username, jwtTokenProvider.getUsername(token));
        assertEquals(tenantId, jwtTokenProvider.getTenantId(token));

        // then - Claims 검증
        Claims claims = jwtTokenProvider.getClaims(token);
        assertEquals(username, claims.getSubject());
        assertEquals(tenantId, claims.get("tenantId", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("다른 만료 시간으로 생성된 토큰 검증")
    void validateToken_WithDifferentExpirationTime_ShouldWorkCorrectly() {
        // given
        JwtTokenProvider longExpirationProvider = new JwtTokenProvider(TEST_SECRET_KEY, 7200); // 2시간
        String token = longExpirationProvider.generateAccessToken(TEST_USERNAME, TEST_TENANT_ID);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then - 같은 시크릿 키로 생성되었으므로 유효해야 함
        assertTrue(isValid);
    }
} 