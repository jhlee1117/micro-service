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
import java.util.Base64;

@Component
public class JwtTokenValidator {

    // private final JwtTokenValidator jwtTokenValidator;

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenValidator.class);

    @Value("${jwt.secret}")
    private String secretKeyString; // JWT 비밀 키

    @Value("${jwt.access-token-expire-time}") // application.yml에 설정된 만료 시간
    private long accessTokenExpirationTime; // 액세스 토큰 만료 시간 (단위: 초)

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpirationTime;

    JwtTokenProvider jwtTokenProvider;

    @PostConstruct
    public void init() {
        logger.info("API Gateway - secretKeyString: '{}'", secretKeyString);
        logger.info("API Gateway - secretKeyString 길이: {}", secretKeyString.length());
        
        jwtTokenProvider = new JwtTokenProvider(secretKeyString, accessTokenExpirationTime, refreshTokenExpirationTime);
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
        // 테스트용 시크릿 키 (실제 환경에서는 Config Server에서 가져와야 함)
        String testSecretKey = "lFMCnn04hFUty4RnSNjDDfQ8eYBySWTWis5AmbGiv6U=";
        
        // JwtTokenProvider 생성
        JwtTokenProvider provider = new JwtTokenProvider(testSecretKey, 3600000, 259200000);
        System.out.println(provider.getActualSecretKey().getAlgorithm());
        // 토큰 생성
        String token = provider.generateAccessToken("admin", "1");
        System.out.println("Generated Token: " + token);
        
        // 토큰 검증
        boolean isValid = provider.validateToken(token);
        System.out.println("Token validation result: " + isValid);
        
        // Claims 추출
        try {
            String username = provider.getUsername(token);
            String tenantId = provider.getTenantId(token);
            System.out.println("Username: " + username);
            System.out.println("TenantId: " + tenantId);
        } catch (Exception e) {
            System.err.println("Error extracting claims: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
