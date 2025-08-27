package com.microservices.auth_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.common.exceptions.NoTenantException;

import com.common.jwt.JwtTokenProvider;
import com.microservices.auth_service.domain.entity.Tenant;
import com.microservices.auth_service.domain.entity.User;
import com.microservices.auth_service.dto.LoginRequest;
import com.microservices.auth_service.dto.LoginResponse;
import com.microservices.auth_service.dto.RegisterRequest;
import com.microservices.auth_service.dto.RegisterResponse;
import com.microservices.auth_service.repository.TenantRepository;
import com.microservices.auth_service.repository.UserRepository;
// import com.microservices.auth_service.exception.UserAlreadyExistsException;
// import com.microservices.auth_service.exception.UserNotFoundException;
// import com.microservices.auth_service.dto.RegisterRequest;
// import com.microservices.auth_service.dto.UserInfo;
// import com.microservices.auth_service.repository.RoleRepository;
// import com.microservices.auth_service.domain.entity.Role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    // @Autowired
    // private RoleRepository roleRepository;

    public LoginResponse authenticate(LoginRequest request, String clientIp) {
        try {
            // Rate Limit 체크
            rateLimitService.checkLoginAttempts(clientIp);
            
            // Spring Security를 통한 인증
            log.info("request.getUsername() : " + request.getUsername());
            log.info("request.getPassword() : " + request.getPassword());
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String username = authentication.getName();
            // 인증 성공 시 Rate Limit 리셋
            rateLimitService.resetLoginAttempts(clientIp);
            
            // 추가 사용자 정보 조회 (JWT 토큰에 포함할 정보)
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            if (user.getTenant() == null || user.getTenant().getId() == null) {
                throw new NoTenantException("Tenant ID is required");
            }

            if (!user.isEnabled()) {
                throw new RuntimeException("User is not enabled");
            }

            Tenant tenant = tenantRepository.findById(user.getTenant().getId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
            
            // JWT 토큰 생성 (실제 사용자 정보 사용)
            String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getTenant().getId().toString());
            String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getTenant().getId().toString());

            refreshTokenService.saveRefreshToken(username, refreshToken, jwtTokenProvider.getRefreshTokenExpirationTime());
            
            // LoginResponse에 사용자 정보 포함
            return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .tenantName(tenant.getName())
                .email(user.getEmail())
                .tenantId(user.getTenant().getId().toString())
                .roles(user.getRoleNames().stream().toList())
                // .message("Login successful")
                .success(true)
                .build();
            
        } catch (Exception e) {
            // 인증 실패 시 Rate Limit은 이미 checkLoginAttempts에서 처리됨
            throw e;
        }
    }

    public RegisterResponse register(RegisterRequest request, String clientIp) {
        try {
            // Rate Limit 체크
            if (rateLimitService.isIpBlocked(clientIp)) {
                return RegisterResponse.failure("IP is blocked due to too many attempts");
            }
            
            // 사용자 정보 검증
            if (userRepository.existsByUsername(request.getUsername())) {
                return RegisterResponse.failure("Username already exists");
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                return RegisterResponse.failure("Email already exists");
            }

            if (!request.isTerms()) {
                return RegisterResponse.failure("Terms approval is required");
            }

            // 사용자 생성 및 저장 (기본 tenantId는 1로 설정)
            User user = User.builder()
                .username(request.getUsername())
                .name(request.getFirstName() + " " + request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                // .tenantId(1L) // 기본 tenant ID 설정
                .enabled(true)
                .build();
            
            userRepository.save(user);
            
            // Rate Limit 리셋 (성공적인 등록 후)
            rateLimitService.resetLoginAttempts(clientIp);
            
            return RegisterResponse.success(user.getId(), user.getUsername());
            
        } catch (Exception e) {
            // 등록 실패 시 로그 기록
            log.error("Registration failed: {}", e.getMessage());
            return RegisterResponse.failure("Registration failed: " + e.getMessage());
        }
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        try {

            // 1. Refresh Token 유효성 검사
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                return LoginResponse.failure("Invalid refresh token");
            } 

            // 2. Refresh Token 저장소 유효성 검사
            if (!refreshTokenService.validateRefreshToken(refreshToken)) {
                return LoginResponse.failure("Refresh token not found in storage");
            }

            // 3. 사용자 정보 조회
            String username = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
            String tenantId = jwtTokenProvider.getTenantIdFromRefreshToken(refreshToken);            

            // 4. 사용자 존재 확인
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            // 5. 새로운 토큰 생성
            String newAccessToken = jwtTokenProvider.generateAccessToken(username, tenantId);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, tenantId);

            // 6. Token Rotation (기존 토큰 무효화, 새 토큰 저장)
            refreshTokenService.rotateRefreshToken(username, refreshToken, newRefreshToken, jwtTokenProvider.getRefreshTokenExpirationTime());


            return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .userId(user.getId())
                .username(user.getUsername())
                .tenantId(user.getTenant() != null && user.getTenant().getId() != null ? user.getTenant().getId().toString() : tenantId)
                .roles(user.getRoleNames().stream().toList())
                .message("Token refreshed successfully")
                .success(true)
            .build();
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return LoginResponse.failure("Token refresh failed: " + e.getMessage());
        }
    }

    public void clearRefreshToken(String refreshToken) {
        
        if (refreshToken != null && !refreshToken.isEmpty()) {
            refreshTokenService.invalidateRefreshToken(refreshToken);
        }
            
    }

    public void blockAccessToken(String accessToken) {
        rateLimitService.addAccessTokenToBlacklist(accessToken);
    }

    /* public void registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }
        
        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .email(request.getEmail())
            .tenantId(request.getTenantId())
            .enabled(true)
            .build();
        
        userRepository.save(user);
    }

    public UserInfo getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        return UserInfo.from(user);
    }

    public void assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RoleNotFoundException("Role not found"));
        
        user.addRole(role, getCurrentUser());
        userRepository.save(user);
    } */
}
