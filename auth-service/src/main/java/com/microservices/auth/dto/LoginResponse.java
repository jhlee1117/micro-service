package com.microservices.auth.dto;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

  private String accessToken;
  private String tokenType = "Bearer";
  private long expiresIn;

  private String refreshToken; // 선택: 리프레시 토큰 전략을 쓰는 경우

  private Long userId;
  private String username;
  // private String userNameKr;

  private String tenantId;
  private String tenantName;
  // private String entityId;

  private List<String> roles;
  private String name;
  private String email;

  private String message;
  private boolean success;
  private List<MenuDto> menuList;

  // 정적 팩토리 메서드
  public static LoginResponse success(Optional<String> token, String message) {
    LoginResponse response = new LoginResponse();
    response.setAccessToken(token.orElse(null));
    response.setMessage(message);
    response.setSuccess(true);
    return response;
  }

  public static LoginResponse failure(String message) {
    LoginResponse response = new LoginResponse();
    response.setMessage(message);
    response.setSuccess(false);
    return response;
  }
}
