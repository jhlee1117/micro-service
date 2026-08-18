package com.microservices.auth.service;

import com.common.exceptions.BusinessException;
import com.common.exceptions.NoTenantException;
import com.common.exceptions.code.TenantErrorCode;
import com.common.exceptions.code.UserErrorCode;
import com.common.jwt.JwtTokenProvider;
import com.common.util.TokenGenerator;
import com.microservices.auth.domain.entity.AuthType;
import com.microservices.auth.domain.entity.OAuthAccount;
import com.microservices.auth.domain.entity.Tenant;
import com.microservices.auth.domain.entity.User;
import com.microservices.auth.dto.LoginResponse;
import com.microservices.auth.dto.RoleDto;
import com.microservices.auth.dto.UserDto;
import com.microservices.auth.oauth.OAuthLoginResult;
import com.microservices.auth.oauth.OAuthPendingSignup;
import com.microservices.auth.oauth.OAuthSignupCompleteRequest;
import com.microservices.auth.oauth.OAuthUserProfile;
import com.microservices.auth.repository.OAuthAccountRepository;
import com.microservices.auth.repository.TenantRepository;
import com.microservices.auth.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OAuthLoginService {

  private final OAuthAccountRepository oauthAccountRepository;
  private final UserRepository userRepository;
  private final TenantRepository tenantRepository;
  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final Duration signupTokenTtl;

  public OAuthLoginService(
      OAuthAccountRepository oauthAccountRepository,
      UserRepository userRepository,
      TenantRepository tenantRepository,
      UserService userService,
      JwtTokenProvider jwtTokenProvider,
      RefreshTokenService refreshTokenService,
      @Value("${app.oauth2.signup-token-ttl:15m}") Duration signupTokenTtl) {
    this.oauthAccountRepository = oauthAccountRepository;
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
    this.userService = userService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.refreshTokenService = refreshTokenService;
    this.signupTokenTtl = signupTokenTtl;
  }

  @Transactional
  public OAuthLoginResult loginOrPrepareSignup(OAuthUserProfile profile) {
    validateProfile(profile);

    return oauthAccountRepository
        .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId())
        .map(OAuthAccount::getUser)
        .map(this::issueTokenOrPendingSignup)
        .orElseGet(() -> linkOrCreateUser(profile));
  }

  private OAuthLoginResult linkOrCreateUser(OAuthUserProfile profile) {
    return userRepository
        .findByEmail(profile.email())
        .map(user -> linkExistingUser(user, profile))
        .orElseGet(() -> createPendingUser(profile));
  }

  private OAuthLoginResult linkExistingUser(User user, OAuthUserProfile profile) {
    createOAuthAccount(user, profile);

    if (user.getAuthType() == AuthType.LOCAL) {
      user.setAuthType(AuthType.BOTH);
    }

    userRepository.save(user);
    return issueTokenOrPendingSignup(user);
  }

  private OAuthLoginResult createPendingUser(OAuthUserProfile profile) {
    User user =
        User.builder()
            .username(generateUsername(profile))
            .email(profile.email())
            .name(profile.name())
            .authType(AuthType.OAUTH)
            .signupCompleted(false)
            .enabled(true)
            .build();

    User savedUser = userRepository.save(user);
    createOAuthAccount(savedUser, profile);

    return OAuthLoginResult.pending(
        savedUser.getId(), savedUser.getEmail(), preparePendingSignup(savedUser));
  }

  private OAuthLoginResult issueTokenOrPendingSignup(User user) {
    if (!user.isSignupCompleted() || user.getTenant() == null || user.getTenant().getId() == null) {
      return OAuthLoginResult.pending(user.getId(), user.getEmail(), preparePendingSignup(user));
    }

    LoginResponse loginResponse = issueLoginTokens(user);
    return OAuthLoginResult.loggedIn(loginResponse);
  }

  @Transactional
  public LoginResponse completeSignup(OAuthSignupCompleteRequest request) {
    String signupTokenHash = TokenGenerator.hashToken(request.signupToken());
    User user =
        userRepository
            .findBySignupTokenHash(signupTokenHash)
            .orElseThrow(() -> new BusinessException(UserErrorCode.INVALID_SIGNUP_TOKEN));

    validateSignupToken(user, request.signupToken());
    validateUsernameAvailable(request.username(), user.getId());

    Tenant tenant =
        tenantRepository
            .findById(request.tenantId())
            .orElseThrow(() -> new BusinessException(TenantErrorCode.TENANT_NOT_FOUND));

    user.setUsername(request.username());
    user.setName(request.name());
    user.setTenant(tenant);
    user.setSignupCompleted(true);
    user.setSignupTokenHash(null);
    user.setSignupTokenExpiresAt(null);
    userRepository.save(user);

    return issueLoginTokens(user);
  }

  private OAuthPendingSignup preparePendingSignup(User user) {
    final Long tenantId = user.getTenant() == null ? null : user.getTenant().getId();
    String signupToken = TokenGenerator.generateOpaqueToken();

    user.setSignupTokenHash(TokenGenerator.hashToken(signupToken));
    user.setSignupTokenExpiresAt(LocalDateTime.now().plus(signupTokenTtl));
    userRepository.save(user);

    return new OAuthPendingSignup(signupToken, user.getUsername(), user.getName(), tenantId);
  }

  private void validateSignupToken(User user, String signupToken) {
    if (user.isSignupCompleted()
        || !TokenGenerator.matches(signupToken, user.getSignupTokenHash())) {
      throw new BusinessException(UserErrorCode.INVALID_SIGNUP_TOKEN);
    }

    if (user.getSignupTokenExpiresAt() == null
        || !user.getSignupTokenExpiresAt().isAfter(LocalDateTime.now())) {
      throw new BusinessException(UserErrorCode.EXPIRED_SIGNUP_TOKEN);
    }
  }

  private void validateUsernameAvailable(String username, Long currentUserId) {
    userRepository
        .findByUsername(username)
        .filter(existingUser -> !existingUser.getId().equals(currentUserId))
        .ifPresent(
            existingUser -> {
              throw new BusinessException(UserErrorCode.ALREADY_EXISTS_USER);
            });
  }

  private LoginResponse issueLoginTokens(User user) {
    UserDto userDto = userService.getUserDtoByUsername(user.getUsername());

    if (userDto.getTenant() == null || userDto.getTenant().getId() == null) {
      throw new NoTenantException("Tenant ID is required");
    }

    String username = userDto.getUsername();
    String tenantId = userDto.getTenant().getId().toString();
    String accessToken = jwtTokenProvider.generateAccessToken(username, tenantId);
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
        .roles(userDto.getRoles().stream().map(RoleDto::getName).toList())
        .success(true)
        .build();
  }

  private void createOAuthAccount(User user, OAuthUserProfile profile) {
    OAuthAccount account =
        OAuthAccount.builder()
            .user(user)
            .provider(profile.provider())
            .providerUserId(profile.providerUserId())
            .providerEmail(profile.email())
            .providerName(profile.name())
            .profileImageUrl(profile.profileImageUrl())
            .build();

    oauthAccountRepository.save(account);
  }

  private String generateUsername(OAuthUserProfile profile) {
    String provider = profile.provider().name().toLowerCase(Locale.ROOT);
    String providerUserId = profile.providerUserId().replaceAll("[^a-zA-Z0-9]", "");
    String baseUsername = provider + "_" + providerUserId;
    String username = baseUsername;
    int suffix = 1;

    while (userRepository.existsByUsername(username)) {
      username = baseUsername + "_" + suffix++;
    }

    return username;
  }

  private void validateProfile(OAuthUserProfile profile) {
    if (!StringUtils.hasText(profile.providerUserId())) {
      throw new IllegalArgumentException("OAuth provider user id is required");
    }

    if (!StringUtils.hasText(profile.email())) {
      throw new IllegalArgumentException("OAuth email is required");
    }
  }
}
