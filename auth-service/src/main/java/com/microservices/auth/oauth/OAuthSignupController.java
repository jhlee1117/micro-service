package com.microservices.auth.oauth;

import com.microservices.auth.dto.LoginResponse;
import com.microservices.auth.service.OAuthLoginService;
import com.microservices.auth.utils.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/oauth/signup")
public class OAuthSignupController {

  private static final int REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS = 2_592_000;

  private final OAuthLoginService oauthLoginService;
  private final CookieUtils cookieUtils;

  public OAuthSignupController(OAuthLoginService oauthLoginService, CookieUtils cookieUtils) {
    this.oauthLoginService = oauthLoginService;
    this.cookieUtils = cookieUtils;
  }

  @PostMapping("/complete")
  public ResponseEntity<LoginResponse> completeSignup(
      @Valid @RequestBody OAuthSignupCompleteRequest request, HttpServletResponse response) {
    LoginResponse loginResponse = oauthLoginService.completeSignup(request);
    cookieUtils.setRefreshTokenCookie(
        response, loginResponse.getRefreshToken(), REFRESH_TOKEN_COOKIE_MAX_AGE_SECONDS);
    loginResponse.setRefreshToken(null);
    return ResponseEntity.ok(loginResponse);
  }
}
