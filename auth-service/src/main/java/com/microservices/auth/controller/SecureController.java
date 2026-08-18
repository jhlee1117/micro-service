package com.microservices.auth.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** JWT 인증이 필요한 보안 엔드포인트 Zero Trust 원칙에 따라 모든 요청에 대해 JWT 토큰 검증 */
@RestController
@RequestMapping("/secure")
public class SecureController {

  @GetMapping("/user-info")
  public ResponseEntity<Map<String, Object>> getUserInfo() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Map<String, Object> response = new HashMap<>();
    response.put("authenticated", authentication != null && authentication.isAuthenticated());
    response.put("username", authentication != null ? authentication.getName() : null);
    response.put("authorities", authentication != null ? authentication.getAuthorities() : null);
    response.put("message", "이 엔드포인트는 JWT 인증이 필요합니다");

    return ResponseEntity.ok(response);
  }

  @GetMapping("/profile")
  public ResponseEntity<Map<String, Object>> getProfile() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Map<String, Object> response = new HashMap<>();
    response.put("service", "auth-service");
    response.put("user", authentication.getName());
    response.put("message", "JWT 인증을 통과한 사용자의 프로필 정보입니다");
    response.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/health-secure")
  public ResponseEntity<Map<String, String>> getSecureHealth() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "healthy");
    response.put("service", "auth-service");
    response.put("security", "jwt-protected");
    response.put("message", "이 헬스체크는 JWT 인증이 필요합니다");

    return ResponseEntity.ok(response);
  }
}
