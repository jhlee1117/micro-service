package com.microservices.auth.service;

import com.common.exceptions.NoTenantException;
import com.common.exceptions.TooManyAttemptsException;
import com.common.jwt.JwtTokenProvider;
import com.microservices.auth.domain.entity.User;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.LoginResponse;
import com.microservices.auth.dto.RegisterRequest;
import com.microservices.auth.dto.RegisterResponse;
import com.microservices.auth.dto.RoleDto;
import com.microservices.auth.dto.UserDto;
import com.microservices.auth.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private JwtTokenProvider jwtTokenProvider;

  @Autowired private UserRepository userRepository;

  @Autowired private RateLimitService rateLimitService;

  @Autowired private UserService userService;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private RefreshTokenService refreshTokenService;

  public LoginResponse authenticate(LoginRequest request, String clientIp) {
    rateLimitService.checkLoginAttempts(clientIp);

    log.info("Login attempt for user: {}", request.getUsername());
    Authentication authentication =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    String username = authentication.getName();
    rateLimitService.resetLoginAttempts(clientIp);

    UserDto userDto = userService.getUserDtoByUsername(username);
    return issueLoginTokens(userDto);
  }

  public LoginResponse issueLoginTokens(User user) {
    UserDto userDto = userService.getUserDtoByUsername(user.getUsername());
    return issueLoginTokens(userDto);
  }

  public LoginResponse issueLoginTokens(UserDto userDto) {
    if (userDto.getTenant() == null || userDto.getTenant().getId() == null) {
      throw new NoTenantException("Tenant ID is required");
    }

    if (!userDto.isActive()) {
      throw new RuntimeException("User is not enabled");
    }

    String username = userDto.getUsername();
    String tenantId = userDto.getTenant().getId().toString();
    String tenantSchema = userDto.getTenant().getName();
    List<String> roles = userDto.getRoles().stream().map(RoleDto::getName).toList();
    boolean isSuperAdmin = roles.contains(SUPER_ADMIN_ROLE);

    String accessToken =
        jwtTokenProvider.generateAccessToken(username, tenantId, tenantSchema, roles);
    String refreshToken = jwtTokenProvider.generateRefreshToken(username, tenantId);

    refreshTokenService.saveRefreshToken(
        username, refreshToken, jwtTokenProvider.getRefreshTokenExpirationTime());

    return LoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(3600)
        .userId(userDto.getId())
        .username(userDto.getUsername())
        .name(userDto.getName())
        .tenantName(userDto.getTenant().getName())
        .email(userDto.getEmail())
        .tenantId(tenantId)
        .tenantSchema(tenantSchema)
        .roles(roles)
        .superAdmin(isSuperAdmin)
        .success(true)
        .build();
  }

  public RegisterResponse register(RegisterRequest request, String clientIp) {
    if (rateLimitService.isIpBlocked(clientIp)) {
      throw new TooManyAttemptsException("IP is blocked due to too many attempts");
    }

    if (userRepository.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists");
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists");
    }

    if (!request.isTerms()) {
      throw new IllegalArgumentException("Terms approval is required");
    }

    User user =
        User.builder()
            .username(request.getUsername())
            .name(request.getFirstName() + " " + request.getLastName())
            .password(passwordEncoder.encode(request.getPassword()))
            .email(request.getEmail())
            .enabled(true)
            .build();

    userRepository.save(user);
    rateLimitService.resetLoginAttempts(clientIp);

    return RegisterResponse.success(user.getId(), user.getUsername());
  }

  public LoginResponse refreshAccessToken(String refreshToken) {
    try {
      if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
        return LoginResponse.failure("Invalid refresh token");
      }

      if (!refreshTokenService.validateRefreshToken(refreshToken)) {
        return LoginResponse.failure("Refresh token not found in storage");
      }

      String username = jwtTokenProvider.getUsernameFromRefreshToken(refreshToken);
      String tenantId = jwtTokenProvider.getTenantIdFromRefreshToken(refreshToken);

      User user =
          userRepository
              .findByUsername(username)
              .orElseThrow(() -> new RuntimeException("User not found after authentication"));

      String tenantSchema = user.getTenant() != null ? user.getTenant().getName() : null;
      List<String> roles = user.getRoleNames().stream().toList();
      boolean isSuperAdmin = roles.contains(SUPER_ADMIN_ROLE);

      String newAccessToken =
          jwtTokenProvider.generateAccessToken(username, tenantId, tenantSchema, roles);
      String newRefreshToken = jwtTokenProvider.generateRefreshToken(username, tenantId);

      refreshTokenService.rotateRefreshToken(
          username,
          refreshToken,
          newRefreshToken,
          jwtTokenProvider.getRefreshTokenExpirationTime());

      return LoginResponse.builder()
          .accessToken(newAccessToken)
          .refreshToken(newRefreshToken)
          .tokenType("Bearer")
          .expiresIn(3600)
          .userId(user.getId())
          .username(user.getUsername())
          .tenantId(
              user.getTenant() != null && user.getTenant().getId() != null
                  ? user.getTenant().getId().toString()
                  : tenantId)
          .tenantSchema(tenantSchema)
          .roles(roles)
          .superAdmin(isSuperAdmin)
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
}
