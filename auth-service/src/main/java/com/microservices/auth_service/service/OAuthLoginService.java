package com.microservices.auth_service.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.common.exceptions.NoTenantException;
import com.common.jwt.JwtTokenProvider;
import com.microservices.auth_service.domain.entity.AuthType;
import com.microservices.auth_service.domain.entity.OAuthAccount;
import com.microservices.auth_service.domain.entity.User;
import com.microservices.auth_service.dto.LoginResponse;
import com.microservices.auth_service.dto.RoleDto;
import com.microservices.auth_service.dto.UserDto;
import com.microservices.auth_service.oauth.OAuthLoginResult;
import com.microservices.auth_service.oauth.OAuthUserProfile;
import com.microservices.auth_service.repository.OAuthAccountRepository;
import com.microservices.auth_service.repository.UserRepository;

@Service
public class OAuthLoginService {

    private final OAuthAccountRepository oauthAccountRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public OAuthLoginService(
        OAuthAccountRepository oauthAccountRepository,
        UserRepository userRepository,
        UserService userService,
        JwtTokenProvider jwtTokenProvider,
        RefreshTokenService refreshTokenService
    ) {
        this.oauthAccountRepository = oauthAccountRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public OAuthLoginResult loginOrPrepareSignup(OAuthUserProfile profile) {
        validateProfile(profile);

        return oauthAccountRepository
            .findByProviderAndProviderUserId(profile.provider(), profile.providerUserId())
            .map(OAuthAccount::getUser)
            .map(this::issueTokenOrSignupRequired)
            .orElseGet(() -> linkOrCreateUser(profile));
    }

    private OAuthLoginResult linkOrCreateUser(OAuthUserProfile profile) {
        return userRepository.findByEmail(profile.email())
            .map(user -> linkExistingUser(user, profile))
            .orElseGet(() -> createPendingUser(profile));
    }

    private OAuthLoginResult linkExistingUser(User user, OAuthUserProfile profile) {
        createOAuthAccount(user, profile);

        if (user.getAuthType() == AuthType.LOCAL) {
            user.setAuthType(AuthType.BOTH);
        }

        userRepository.save(user);
        return issueTokenOrSignupRequired(user);
    }

    private OAuthLoginResult createPendingUser(OAuthUserProfile profile) {
        User user = User.builder()
            .username(generateUsername(profile))
            .email(profile.email())
            .name(profile.name())
            .authType(AuthType.OAUTH)
            .signupCompleted(false)
            .enabled(true)
            .build();

        User savedUser = userRepository.save(user);
        createOAuthAccount(savedUser, profile);

        return OAuthLoginResult.signupRequired(savedUser.getId(), savedUser.getEmail());
    }

    private OAuthLoginResult issueTokenOrSignupRequired(User user) {
        if (!user.isSignupCompleted() || user.getTenant() == null || user.getTenant().getId() == null) {
            return OAuthLoginResult.signupRequired(user.getId(), user.getEmail());
        }

        LoginResponse loginResponse = issueLoginTokens(user);
        return OAuthLoginResult.loggedIn(loginResponse);
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

        refreshTokenService.saveRefreshToken(username, refreshToken, jwtTokenProvider.getRefreshTokenExpirationTime());

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
        OAuthAccount account = OAuthAccount.builder()
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
