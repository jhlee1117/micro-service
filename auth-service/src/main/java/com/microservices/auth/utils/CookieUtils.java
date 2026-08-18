package com.microservices.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

  @Value("${app.cookie.domain:localhost}")
  private String cookieDomain;

  @Value("${app.cookie.secure:false}")
  private boolean cookieSecure;

  public void setRefreshTokenCookie(HttpServletResponse response, String token, int maxAge) {
    Cookie cookie = new Cookie("refresh_token", token);
    cookie.setHttpOnly(true);
    cookie.setSecure(cookieSecure);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);

    if (!"localhost".equals(cookieDomain)) {
      cookie.setDomain(cookieDomain);
    }

    response.addCookie(cookie);
  }

  public void clearTokenCookies(HttpServletResponse response) {
    Cookie refreshCookie = new Cookie("refresh_token", null);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(cookieSecure);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(0);

    if (!"localhost".equals(cookieDomain)) {
      refreshCookie.setDomain(cookieDomain);
    }

    response.addCookie(refreshCookie);
  }

  public String getRefreshToken(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("refresh_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return "";
  }
}
