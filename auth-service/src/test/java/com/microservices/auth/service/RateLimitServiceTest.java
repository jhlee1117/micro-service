package com.microservices.auth.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.common.exceptions.TooManyAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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

    // First login attempt should succeed
    assertDoesNotThrow(() -> rateLimitService.checkLoginAttempts(testIp));
    assertEquals(1, rateLimitService.getCurrentAttempts(testIp));
  }

  @Test
  void testMultipleLoginAttempts() {
    String testIp = "192.168.1.2";

    // 5 attempts should succeed
    for (int i = 0; i < 5; i++) {
      assertDoesNotThrow(() -> rateLimitService.checkLoginAttempts(testIp));
    }

    assertEquals(5, rateLimitService.getCurrentAttempts(testIp));

    // 6th attempt should be blocked
    assertThrows(TooManyAttemptsException.class, () -> rateLimitService.checkLoginAttempts(testIp));
  }

  @Test
  void testResetLoginAttempts() {
    String testIp = "192.168.1.3";

    // Two attempts
    rateLimitService.checkLoginAttempts(testIp);
    rateLimitService.checkLoginAttempts(testIp);
    assertEquals(2, rateLimitService.getCurrentAttempts(testIp));

    // Reset
    rateLimitService.resetLoginAttempts(testIp);
    assertEquals(0, rateLimitService.getCurrentAttempts(testIp));
  }

  @Test
  void testBlockedIpStatus() {
    String testIp = "192.168.1.4";

    // Exceed maximum attempt count
    for (int i = 0; i < 6; i++) {
      try {
        rateLimitService.checkLoginAttempts(testIp);
      } catch (TooManyAttemptsException e) {
        // Expected exception
      }
    }

    // Check remaining time
    long remainingTime = rateLimitService.getRemainingBlockTime(testIp);
    assertTrue(remainingTime > 0, "IP should be blocked");
  }

  @Test
  void testDifferentIps() {
    String ip1 = "192.168.1.5";
    String ip2 = "192.168.1.6";

    // IP1 has 3 attempts
    for (int i = 0; i < 3; i++) {
      rateLimitService.checkLoginAttempts(ip1);
    }

    // IP2 has 2 attempts
    for (int i = 0; i < 2; i++) {
      rateLimitService.checkLoginAttempts(ip2);
    }

    assertEquals(3, rateLimitService.getCurrentAttempts(ip1));
    assertEquals(2, rateLimitService.getCurrentAttempts(ip2));
  }
}
