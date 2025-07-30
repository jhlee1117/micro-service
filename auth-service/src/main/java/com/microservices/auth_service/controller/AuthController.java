package com.microservices.auth_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.exceptions.NoTenantException;
import com.common.exceptions.TooManyAttemptsException;
import com.microservices.auth_service.dto.LoginRequest;
import com.microservices.auth_service.dto.LoginResponse;
import com.microservices.auth_service.dto.RegisterRequest;
import com.microservices.auth_service.dto.RegisterResponse;
import com.microservices.auth_service.service.AuthService;
import static com.microservices.auth_service.utils.WebUtils.getClientIp;
import com.microservices.auth_service.service.RateLimitService;
import com.microservices.auth_service.utils.CookieUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Optional;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication API", description = "인증 API 문서")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private CookieUtils cookieUtils;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Operation(summary = "사용자 로그인", description = "사용자명과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "요청 제한 초과",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "요청 오류",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        )
    })  
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            // Rate limiting을 위한 IP 추출
            String clientIp = getClientIp(httpRequest);
            log.info("Login attempt from IP: {} for user: {}", clientIp, request.getUsername());
            
            LoginResponse response = authService.authenticate(request, clientIp);
            if (response.isSuccess()) {
                cookieUtils.setRefreshTokenCookie(httpResponse, response.getRefreshToken(), 2592000); // 30일

                response.setRefreshToken(null);

                log.info("Refresh token cookie set for user: {}", request.getUsername());
            }
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.failure("Invalid credentials"));
        } catch (TooManyAttemptsException e) {
            log.warn("Too many login attempts from IP: {}", getClientIp(httpRequest));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(LoginResponse.failure("Too many login attempts. Please try again later."));
        } catch (NoTenantException e) {
            log.warn("No tenant ID found for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(LoginResponse.failure("Tenant ID is required"));
        }
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 Access Token을 갱신합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            String refreshToken = cookieUtils.getRefreshToken(httpRequest);
            if (refreshToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(LoginResponse.failure("No refresh token provided"));
            }

            LoginResponse response = authService.refreshAccessToken(refreshToken);
            if (response.isSuccess()) {
                log.info("Token refreshed successfully for user: {}", response.getUsername());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to refresh token: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(response);
            }
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failure("Internal server error: " + e.getMessage()));
        }
    }

    @Operation(summary = "사용자 등록", description = "사용자명과 비밀번호로 등록합니다.")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);

        try {
            RegisterResponse response = authService.register(request, clientIp);
            
            if (response.isSuccess()) {
                log.info("User {} registered successfully", request.getUsername());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Failed to register user: {}", response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Failed to register user", e);
            RegisterResponse errorResponse = RegisterResponse.failure("Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "사용자 로그아웃", description = "사용자를 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<LoginResponse> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            // 1. Refresh Token 쿠키에서 조회
            String refreshToken = cookieUtils.getRefreshToken(httpRequest);
            // 2. Refresh Token 저장소에서 무효화
            authService.clearRefreshToken(refreshToken);
            // 3. Cookie에서 토큰 제거
            cookieUtils.clearTokenCookies(httpResponse);

            // 로그아웃 시 accessToken을 블랙리스트에 추가하는 로직입니다.
            // 1. Authorization 헤더에서 accessToken 추출
            String accessToken = null;
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }

            // 2. accessToken이 존재하면 블랙리스트에 추가
            if (accessToken != null && !accessToken.isEmpty()) {
                try {
                    authService.blockAccessToken(accessToken);
                    log.info("Access token 블랙리스트에 추가 완료");
                } catch (Exception ex) {
                    log.warn("Access token 블랙리스트 추가 실패: {}", ex.getMessage());
                }
            } else {
                log.info("Authorization 헤더에 accessToken이 없어 블랙리스트 추가를 건너뜀");
            }

            // 4. 로그아웃 성공 응답  
            log.info("User logged out successfully");
            return ResponseEntity.ok(LoginResponse.success(Optional.empty(), "Logout successful"));
            
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failure("Logout failed"));
        }
    }

    @GetMapping("/rate-limit/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(HttpServletRequest httpRequest) {
        Map<String, Object> status = new HashMap<>();
        
        String clientIp = getClientIp(httpRequest);
        int currentAttempts = rateLimitService.getCurrentAttempts(clientIp);
        long remainingBlockTime = rateLimitService.getRemainingBlockTime(clientIp);
        boolean isRedisAvailable = rateLimitService.isRedisAvailable();
        
        status.put("ip", clientIp);
        status.put("currentAttempts", currentAttempts);
        status.put("remainingBlockTime", remainingBlockTime);
        status.put("isBlocked", remainingBlockTime > 0);
        status.put("redisAvailable", isRedisAvailable);
        
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/rate-limit/unblock")
    public ResponseEntity<Map<String, Object>> unblockIp(HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        String clientIp = getClientIp(httpRequest);
        
        try {            
            rateLimitService.unblockIp(clientIp);
            response.put("success", true);
            response.put("message", "IP " + clientIp + " has been unblocked");
            log.info("IP {} has been manually unblocked", clientIp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to unblock IP: " + e.getMessage());
            log.error("Failed to unblock IP: {}", clientIp, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/rate-limit/redis-status")
    public ResponseEntity<Map<String, Object>> getRedisStatus() {
        Map<String, Object> status = new HashMap<>();
        
        boolean isAvailable = rateLimitService.isRedisAvailable();
        status.put("redisAvailable", isAvailable);
        status.put("message", isAvailable ? "Redis is connected and working" : "Redis is not available");
        
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/rate-limit/clear-all")
    public ResponseEntity<Map<String, Object>> clearAllRateLimitData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            rateLimitService.clearAllRateLimitData();
            response.put("success", true);
            response.put("message", "All rate limit data has been cleared");
            log.warn("All rate limit data has been cleared manually");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clear rate limit data: " + e.getMessage());
            log.error("Failed to clear rate limit data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Auth Service!";
    }

    @GetMapping("/test")
    public String test() {
        return "Test endpoint is working!";
    }
}
