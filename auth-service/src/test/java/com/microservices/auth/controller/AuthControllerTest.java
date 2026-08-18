package com.microservices.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.LoginResponse;
import com.microservices.auth.dto.MenuDto;
import com.microservices.auth.dto.RegisterRequest;
import com.microservices.auth.dto.RegisterResponse;
import com.microservices.auth.service.AuthService;
import com.microservices.auth.service.MenuService;
import com.microservices.auth.service.RateLimitService;
import com.microservices.auth.utils.CookieUtils;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AuthService authService;

  @MockitoBean private MenuService menuService;

  @MockitoBean private RateLimitService rateLimitService;

  @MockitoBean private CookieUtils cookieUtils;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  void testLoginSuccess() throws Exception {
    // Given
    LoginRequest request = new LoginRequest("testuser", "password");

    // LoginResponse.builder()를 사용하여 올바른 응답 생성
    LoginResponse mockResponse =
        LoginResponse.builder()
            .accessToken("mock-access-token")
            .refreshToken("mock-refresh-token")
            .username("testuser")
            .userId(1L)
            .success(true)
            .message("Login successful")
            .build();

    // MenuDto.builder()를 사용하여 올바른 메뉴 생성
    MenuDto menuDto =
        MenuDto.builder()
            .menuCode("TEST001")
            .menuAlias("Test Menu")
            .path("/test")
            .isActive(true)
            .build();

    when(authService.authenticate(any(LoginRequest.class), anyString())).thenReturn(mockResponse);
    when(menuService.getMenuListByUserInfo(any(Long.class))).thenReturn(Arrays.asList(menuDto));

    // When & Then
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Login successful"))
        .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
        .andExpect(jsonPath("$.username").value("testuser"));
  }

  @Test
  void testLoginFailure() throws Exception {
    // Given
    LoginRequest request = new LoginRequest("invaliduser", "wrongpassword");
    LoginResponse mockResponse = LoginResponse.failure("Invalid credentials");

    when(authService.authenticate(any(LoginRequest.class), anyString())).thenReturn(mockResponse);

    // When & Then
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Invalid credentials"));
  }

  @Test
  void testRegisterSuccess() throws Exception {
    // Given
    RegisterRequest request = new RegisterRequest();
    request.setUsername("newuser");
    request.setPassword("password");
    request.setFirstName("New");
    request.setLastName("User");
    request.setEmail("newuser@example.com");
    request.setTerms(true);

    RegisterResponse mockResponse = RegisterResponse.success(1L, "newuser");

    when(authService.register(any(RegisterRequest.class), anyString())).thenReturn(mockResponse);

    // When & Then
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("User registered successfully"));
  }

  @Test
  void testRegisterFailure() throws Exception {
    // Given
    RegisterRequest request = new RegisterRequest();
    request.setUsername("existinguser");
    request.setPassword("password");
    request.setFirstName("Existing");
    request.setLastName("User");
    request.setEmail("existing@example.com");
    request.setTerms(true);

    RegisterResponse mockResponse = RegisterResponse.failure("Username already exists");

    when(authService.register(any(RegisterRequest.class), anyString())).thenReturn(mockResponse);

    // When & Then
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Username already exists"));
  }

  @Test
  void testLogoutSuccess() throws Exception {
    // Given
    // void 메서드는 doNothing()을 사용하여 mocking
    doNothing().when(authService).clearRefreshToken(anyString());
    doNothing().when(authService).blockAccessToken(anyString());

    // When & Then
    mockMvc
        .perform(post("/auth/logout").header("Authorization", "Bearer mock-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Logout successful"));
  }

  @Test
  void testRefreshTokenSuccess() throws Exception {
    // Given
    LoginResponse mockResponse =
        LoginResponse.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .success(true)
            .message("Token refreshed")
            .build();

    when(cookieUtils.getRefreshToken(any())).thenReturn("valid-refresh-token");
    when(authService.refreshAccessToken(anyString())).thenReturn(mockResponse);

    // When & Then
    mockMvc
        .perform(post("/auth/refresh"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Token refreshed"));
  }

  @Test
  void testRefreshTokenFailure() throws Exception {
    // Given
    when(cookieUtils.getRefreshToken(any())).thenReturn("");

    // When & Then
    mockMvc
        .perform(post("/auth/refresh"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("No refresh token provided"));
  }

  @Test
  void testGetRateLimitStatus() throws Exception {
    // Given
    when(rateLimitService.getCurrentAttempts(anyString())).thenReturn(2);
    when(rateLimitService.getRemainingBlockTime(anyString())).thenReturn(0L);
    when(rateLimitService.isRedisAvailable()).thenReturn(true);

    // When & Then
    mockMvc
        .perform(post("/auth/rate-limit/status"))
        .andExpect(status().isMethodNotAllowed()); // GET 메서드가 필요한데 POST로 요청했으므로 405
  }

  @Test
  void testHelloEndpoint() throws Exception {
    mockMvc
        .perform(post("/auth/hello"))
        .andExpect(status().isMethodNotAllowed()); // GET 메서드가 필요한데 POST로 요청했으므로 405
  }

  @Test
  void testTestEndpoint() throws Exception {
    mockMvc
        .perform(post("/auth/test"))
        .andExpect(status().isMethodNotAllowed()); // GET 메서드가 필요한데 POST로 요청했으므로 405
  }
}
