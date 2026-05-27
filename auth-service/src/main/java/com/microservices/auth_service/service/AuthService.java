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

import com.common.exceptions.TooManyAttemptsException;
import com.common.jwt.JwtTokenProvider;
import com.microservices.auth_service.domain.entity.User;
import com.microservices.auth_service.dto.LoginRequest;
import com.microservices.auth_service.dto.LoginResponse;
import com.microservices.auth_service.dto.RegisterRequest;
import com.microservices.auth_service.dto.RegisterResponse;
import com.microservices.auth_service.dto.RoleDto;
import com.microservices.auth_service.dto.UserDto;
import com.microservices.auth_service.repository.TenantRepository;
import com.microservices.auth_service.repository.UserRepository;
// import com.microservices.auth_service.exception.UserAlreadyExistsException;
// import com.microservices.auth_service.exception.UserNotFoundException;
// import com.microservices.auth_service.dto.RegisterRequest;
// import com.microservices.auth_service.dto.UserInfo;
// import com.microservices.auth_service.repository.RoleRepository;
// import com.microservices.auth_service.domain.entity.Role;

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
    private RateLimitService rateLimitService;

    @Autowired
    private UserService userService;

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
            
            // 캐시된 DTO 사용 (DB 조회 대신 캐시 바구니 확인)
            UserDto userDto = userService.getUserDtoByUsername(username);

            if (userDto.getTenant() == null || userDto.getTenant().getId() == null) {
                throw new NoTenantException("Tenant ID is required");
            }

            if (!userDto.isActive()) {
                throw new RuntimeException("User is not enabled");
            }
            
            // JWT 토큰 생성
            String accessToken = jwtTokenProvider.generateAccessToken(userDto.getUsername(), userDto.getTenant().getId().toString());
            String refreshToken = jwtTokenProvider.generateRefreshToken(userDto.getUsername(), userDto.getTenant().getId().toString());

            refreshTokenService.saveRefreshToken(username, refreshToken, jwtTokenProvider.getRefreshTokenExpirationTime());
            
            // LoginResponse에 정보 포함
            return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .userId(userDto.getId()) // 실제 ID 전달
                .username(userDto.getUsername())
                .name(userDto.getName())
                .tenantName(userDto.getTenant().getName())
                .email(userDto.getEmail())
                .tenantId(userDto.getTenant().getId().toString())
                .roles(userDto.getRoles().stream().map(RoleDto::getName).toList())
                .success(true)
                .build();
            
        } catch (Exception e) {
            // 인증 실패 시 Rate Limit은 이미 checkLoginAttempts에서 처리됨
            throw e;
        }
    }

    public RegisterResponse register(RegisterRequest request, String clientIp) {
        // Rate Limit 체크
        if (rateLimitService.isIpBlocked(clientIp)) {
            throw new TooManyAttemptsException("IP is blocked due to too many attempts");
        }
        
        // 사용자 정보 검증
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (!request.isTerms()) {
            throw new IllegalArgumentException("Terms approval is required");
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
