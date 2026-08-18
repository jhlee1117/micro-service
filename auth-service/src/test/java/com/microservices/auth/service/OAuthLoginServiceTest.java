package com.microservices.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.common.exceptions.BusinessException;
import com.common.exceptions.code.UserErrorCode;
import com.common.jwt.JwtTokenProvider;
import com.common.util.TokenGenerator;
import com.microservices.auth.domain.entity.Tenant;
import com.microservices.auth.domain.entity.User;
import com.microservices.auth.dto.LoginResponse;
import com.microservices.auth.dto.TenantDto;
import com.microservices.auth.dto.UserDto;
import com.microservices.auth.oauth.OAuthSignupCompleteRequest;
import com.microservices.auth.repository.OAuthAccountRepository;
import com.microservices.auth.repository.TenantRepository;
import com.microservices.auth.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuthLoginServiceTest {

  @Mock private OAuthAccountRepository oauthAccountRepository;
  @Mock private UserRepository userRepository;
  @Mock private TenantRepository tenantRepository;
  @Mock private UserService userService;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private RefreshTokenService refreshTokenService;

  private OAuthLoginService oauthLoginService;

  @BeforeEach
  void setUp() {
    oauthLoginService =
        new OAuthLoginService(
            oauthAccountRepository,
            userRepository,
            tenantRepository,
            userService,
            jwtTokenProvider,
            refreshTokenService,
            Duration.ofMinutes(15));
  }

  @Test
  void completesPendingSignupAndConsumesToken() {
    String signupToken = TokenGenerator.generateOpaqueToken();
    Tenant tenant = Tenant.builder().id(10L).name("tenant").status(true).build();
    User pendingUser =
        User.builder()
            .id(1L)
            .username("temporary")
            .email("oauth@example.com")
            .name("OAuth User")
            .signupCompleted(false)
            .signupTokenHash(TokenGenerator.hashToken(signupToken))
            .signupTokenExpiresAt(LocalDateTime.now().plusMinutes(10))
            .enabled(true)
            .build();
    OAuthSignupCompleteRequest request =
        new OAuthSignupCompleteRequest(signupToken, "final-user", "Final User", tenant.getId());

    UserDto completedUser =
        new UserDto(
            1L,
            "final-user",
            "oauth@example.com",
            "Final User",
            null,
            true,
            Collections.emptySet(),
            new TenantDto(10L, "tenant", true));

    when(userRepository.findBySignupTokenHash(TokenGenerator.hashToken(signupToken)))
        .thenReturn(Optional.of(pendingUser));
    when(userRepository.findByUsername("final-user")).thenReturn(Optional.empty());
    when(tenantRepository.findById(10L)).thenReturn(Optional.of(tenant));
    when(userService.getUserDtoByUsername("final-user")).thenReturn(completedUser);
    when(jwtTokenProvider.generateAccessToken("final-user", "10")).thenReturn("access-token");
    when(jwtTokenProvider.generateRefreshToken("final-user", "10")).thenReturn("refresh-token");
    when(jwtTokenProvider.getRefreshTokenExpirationTime()).thenReturn(3_600L);

    final LoginResponse response = oauthLoginService.completeSignup(request);

    assertTrue(pendingUser.isSignupCompleted());
    assertEquals("final-user", pendingUser.getUsername());
    assertEquals("Final User", pendingUser.getName());
    assertEquals(tenant, pendingUser.getTenant());
    assertNull(pendingUser.getSignupTokenHash());
    assertNull(pendingUser.getSignupTokenExpiresAt());
    assertEquals("access-token", response.getAccessToken());
    verify(userRepository).save(pendingUser);
    verify(refreshTokenService).saveRefreshToken("final-user", "refresh-token", 3_600L);
  }

  @Test
  void rejectsUnknownSignupToken() {
    String signupToken = TokenGenerator.generateOpaqueToken();
    when(userRepository.findBySignupTokenHash(TokenGenerator.hashToken(signupToken)))
        .thenReturn(Optional.empty());

    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () ->
                oauthLoginService.completeSignup(
                    new OAuthSignupCompleteRequest(signupToken, "user", "User", 1L)));

    assertEquals(UserErrorCode.INVALID_SIGNUP_TOKEN, exception.getErrorCode());
  }

  @Test
  void rejectsExpiredSignupToken() {
    String signupToken = TokenGenerator.generateOpaqueToken();
    User pendingUser =
        User.builder()
            .id(1L)
            .username("temporary")
            .signupCompleted(false)
            .signupTokenHash(TokenGenerator.hashToken(signupToken))
            .signupTokenExpiresAt(LocalDateTime.now().minusSeconds(1))
            .build();

    when(userRepository.findBySignupTokenHash(TokenGenerator.hashToken(signupToken)))
        .thenReturn(Optional.of(pendingUser));

    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () ->
                oauthLoginService.completeSignup(
                    new OAuthSignupCompleteRequest(signupToken, "user", "User", 1L)));

    assertEquals(UserErrorCode.EXPIRED_SIGNUP_TOKEN, exception.getErrorCode());
    assertFalse(pendingUser.isSignupCompleted());
  }
}
