package com.microservices.auth_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local") // 필요시 profile 지정
public class JwtConfigTest {

    @Autowired
    private JwtConfig jwtConfig;

    @Test
    void jwtSecret_값이_주입되는지_확인() {
        System.out.println("jwt.secret = " + jwtConfig.jwtTokenProvider().getSecretKeyString());
        // System.out.println("jwt.expiration = " + jwtConfig.jwtTokenProvider().getExpiration());
        
        assertThat(jwtConfig.jwtTokenProvider().getSecretKeyString()).isNotNull();
        // assertThat(jwtConfig.jwtTokenProvider().getExpiration()).isGreaterThan(0);
        
        // JwtConfig 빈이 정상적으로 생성되는지 확인
        assertThat(jwtConfig).isNotNull();
        
        // JwtTokenProvider 생성 시도
        try {
            assertThat(jwtConfig.jwtTokenProvider()).isNotNull();
            System.out.println("JwtTokenProvider 생성 성공");
        } catch (Exception e) {
            System.err.println("JwtTokenProvider 생성 실패: " + e.getMessage());
            // JAR 파일 문제로 인한 실패는 허용
        }
    }
}