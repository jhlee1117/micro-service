package com.microservices.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.common.jwt.JwtTokenProvider;
import com.microservices.auth.domain.entity.Tenant;
import com.microservices.auth.domain.entity.User;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.LoginResponse;
import com.microservices.auth.repository.TenantRepository;
import com.microservices.auth.repository.UserRepository;
import java.util.HashSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private TenantRepository tenantRepository;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private AuthenticationManager authenticationManager;

  @Mock private Authentication authentication;

  @Mock private RefreshTokenService refreshTokenService;

  @Mock private RateLimitService rateLimitService;

  @InjectMocks private AuthService authService;

  private LoginRequest validLoginRequest;
  private User mockUser;
  private Tenant mockTenant;

  @BeforeEach
  void setUp() {
    validLoginRequest = new LoginRequest("testuser", "password");

    mockTenant = Tenant.builder().id(1L).name("TEST Tenant").status(true).build();

    mockUser =
        User.builder()
            .id(1L)
            .username("testuser")
            .name("TEST User")
            .email("test@example.com")
            .enabled(true)
            .tenant(mockTenant)
            .userRoles(new HashSet<>())
            .build();
  }

  @Test
  void testAuthenticateSuccess() throws Exception {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(authentication.getName()).thenReturn("testuser");
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
    when(tenantRepository.findById(1L)).thenReturn(Optional.of(mockTenant));
    when(jwtTokenProvider.generateAccessToken(anyString(), anyString(), any(), anyList()))
        .thenReturn("mock-access-token");
    when(jwtTokenProvider.generateRefreshToken("testuser", "1")).thenReturn("mock-refresh-token");
    when(jwtTokenProvider.getRefreshTokenExpirationTime()).thenReturn(3600L);

    LoginResponse response = authService.authenticate(validLoginRequest, "192.168.1.1");

    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertEquals("mock-access-token", response.getAccessToken());
    assertEquals("mock-refresh-token", response.getRefreshToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(3600, response.getExpiresIn());
    assertEquals("testuser", response.getUsername());
    assertEquals(1L, response.getUserId());
    assertEquals("Test User", response.getName());
    assertEquals("Test Tenant", response.getTenantName());
    assertEquals("test@example.com", response.getEmail());
    assertEquals("1", response.getTenantId());
    assertEquals(2, response.getRoles().size());
    assertTrue(response.getRoles().contains("USER"));
    assertTrue(response.getRoles().contains("ADMIN"));
  }
}
