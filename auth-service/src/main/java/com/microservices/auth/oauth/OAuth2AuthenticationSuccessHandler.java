package com.microservices.auth.oauth;

import com.microservices.auth.domain.entity.OAuthProvider;
import com.microservices.auth.dto.LoginResponse;
import com.microservices.auth.service.OAuthLoginService;
import com.microservices.auth.utils.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final OAuthUserProfileExtractorRegistry profileExtractorRegistry;
  private final OAuthLoginService oauthLoginService;
  private final CookieUtils cookieUtils;
  private final String frontendUrl;
  private final String successPath;
  private final String signupPath;

  public OAuth2AuthenticationSuccessHandler(
      OAuthUserProfileExtractorRegistry profileExtractorRegistry,
      OAuthLoginService oauthLoginService,
      CookieUtils cookieUtils,
      @Value("${frontend.url:http://localhost:3000}") String frontendUrl,
      @Value("${frontend.oauth2.success-path:/oauth2/success}") String successPath,
      @Value("${frontend.oauth2.signup-path:/oauth2/signup}") String signupPath) {
    this.profileExtractorRegistry = profileExtractorRegistry;
    this.oauthLoginService = oauthLoginService;
    this.cookieUtils = cookieUtils;
    this.frontendUrl = frontendUrl;
    this.successPath = successPath;
    this.signupPath = signupPath;
  }

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    OAuthProvider provider =
        OAuthProvider.fromRegistrationId(oauthToken.getAuthorizedClientRegistrationId());
    OAuthUserProfile profile =
        profileExtractorRegistry.extract(provider, oauthToken.getPrincipal().getAttributes());
    OAuthLoginResult result = oauthLoginService.loginOrPrepareSignup(profile);

    if (result.status() == OAuthLoginStatus.PENDING) {
      response.sendRedirect(signupRedirectUrl(result));
      return;
    }

    LoginResponse loginResponse = result.loginResponse();
    cookieUtils.setRefreshTokenCookie(response, loginResponse.getRefreshToken(), 2592000);
    response.sendRedirect(successRedirectUrl(loginResponse));
  }

  private String successRedirectUrl(LoginResponse loginResponse) {
    return UriComponentsBuilder.fromUriString(frontendUrl + successPath)
        .queryParam("status", "success")
        .queryParam("user_id", loginResponse.getUserId())
        .build()
        .encode()
        .toUriString();
  }

  private String signupRedirectUrl(OAuthLoginResult result) {
    OAuthPendingSignup pendingSignup = result.pendingSignup();

    return UriComponentsBuilder.fromUriString(frontendUrl + signupPath)
        //            .queryParam("user_id", result.userId())
        //            .queryParam("email", result.email())
        .queryParam("status", "pending")
        .queryParam("signup_token", pendingSignup.signupToken())
        .queryParam("name", pendingSignup.name())
        .build()
        .encode()
        .toUriString();
  }
}
