package com.common.jwt.config;

import com.common.jwt.JwtTokenProvider;
import com.common.jwt.authentication.JwtAuthenticationHandler;
import com.common.jwt.authentication.TokenBlacklistService;
import com.common.jwt.filter.ReactiveJwtAuthenticationFilter;
import com.common.jwt.filter.ServletJwtAuthenticationFilter;

/** JWT 필터 설정을 위한 유틸리티 클래스 다양한 환경에서 JWT 필터를 쉽게 생성하고 설정할 수 있도록 도와주는 팩토리 클래스 */
public class JwtFilterConfigurer {

  /** Reactive 환경용 JWT 필터 생성 (블랙리스트 서비스 포함) */
  public static ReactiveJwtAuthenticationFilter createReactiveFilter(
      JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {

    JwtAuthenticationHandler handler =
        new JwtAuthenticationHandler(jwtTokenProvider, tokenBlacklistService);
    return new ReactiveJwtAuthenticationFilter(handler, tokenBlacklistService);
  }

  /** Reactive 환경용 JWT 필터 생성 (블랙리스트 서비스 없음) */
  public static ReactiveJwtAuthenticationFilter createReactiveFilter(
      JwtTokenProvider jwtTokenProvider) {

    JwtAuthenticationHandler handler = new JwtAuthenticationHandler(jwtTokenProvider);
    return new ReactiveJwtAuthenticationFilter(handler);
  }

  /** Servlet 환경용 JWT 필터 생성 (블랙리스트 서비스 포함) */
  public static ServletJwtAuthenticationFilter createServletFilter(
      JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {

    JwtAuthenticationHandler handler =
        new JwtAuthenticationHandler(jwtTokenProvider, tokenBlacklistService);
    return new ServletJwtAuthenticationFilter(handler, tokenBlacklistService);
  }

  /** Servlet 환경용 JWT 필터 생성 (블랙리스트 서비스 없음) */
  public static ServletJwtAuthenticationFilter createServletFilter(
      JwtTokenProvider jwtTokenProvider) {

    JwtAuthenticationHandler handler = new JwtAuthenticationHandler(jwtTokenProvider);
    return new ServletJwtAuthenticationFilter(handler);
  }

  /** JWT 인증 핸들러 생성 (블랙리스트 서비스 포함) */
  public static JwtAuthenticationHandler createAuthenticationHandler(
      JwtTokenProvider jwtTokenProvider, TokenBlacklistService tokenBlacklistService) {

    return new JwtAuthenticationHandler(jwtTokenProvider, tokenBlacklistService);
  }

  /** JWT 인증 핸들러 생성 (블랙리스트 서비스 없음) */
  public static JwtAuthenticationHandler createAuthenticationHandler(
      JwtTokenProvider jwtTokenProvider) {

    return new JwtAuthenticationHandler(jwtTokenProvider);
  }
}
