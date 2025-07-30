package com.microservices.auth_service.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.common.exceptions.TooManyAttemptsException;

@SpringBootTest
public class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    void testFirstLoginAttempt() {
        String testIp = "192.168.1.1";
        
        // 첫 번째 시도는 성공해야 함
        assertDoesNotThrow(() -> rateLimitService.checkLoginAttempts(testIp));
        assertEquals(1, rateLimitService.getCurrentAttempts(testIp));
    }

    @Test
    void testMultipleLoginAttempts() {
        String testIp = "192.168.1.2";
        
        // 5번까지는 성공해야 함
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> rateLimitService.checkLoginAttempts(testIp));
        }
        
        assertEquals(5, rateLimitService.getCurrentAttempts(testIp));
        
        // 6번째 시도는 차단되어야 함
        assertThrows(TooManyAttemptsException.class, () -> 
            rateLimitService.checkLoginAttempts(testIp));
    }

    @Test
    void testResetLoginAttempts() {
        String testIp = "192.168.1.3";
        
        // 몇 번 시도
        rateLimitService.checkLoginAttempts(testIp);
        rateLimitService.checkLoginAttempts(testIp);
        assertEquals(2, rateLimitService.getCurrentAttempts(testIp));
        
        // 리셋
        rateLimitService.resetLoginAttempts(testIp);
        assertEquals(0, rateLimitService.getCurrentAttempts(testIp));
    }

    @Test
    void testBlockedIpStatus() {
        String testIp = "192.168.1.4";
        
        // 최대 시도 횟수 초과로 차단
        for (int i = 0; i < 6; i++) {
            try {
                rateLimitService.checkLoginAttempts(testIp);
            } catch (TooManyAttemptsException e) {
                // 예상된 예외
            }
        }
        
        // 차단 시간 확인
        long remainingTime = rateLimitService.getRemainingBlockTime(testIp);
        assertTrue(remainingTime > 0, "IP should be blocked");
    }

    @Test
    void testDifferentIps() {
        String ip1 = "192.168.1.5";
        String ip2 = "192.168.1.6";
        
        // IP1에서 3번 시도
        for (int i = 0; i < 3; i++) {
            rateLimitService.checkLoginAttempts(ip1);
        }
        
        // IP2에서 2번 시도
        for (int i = 0; i < 2; i++) {
            rateLimitService.checkLoginAttempts(ip2);
        }
        
        assertEquals(3, rateLimitService.getCurrentAttempts(ip1));
        assertEquals(2, rateLimitService.getCurrentAttempts(ip2));
    }
} 