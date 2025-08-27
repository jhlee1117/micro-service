package com.microservices.auth_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponse {
    private boolean success;
    private String message;
    private Long userId;
    private String username;
    
    public static RegisterResponse success(Long userId, String username) {
        return RegisterResponse.builder()
            .success(true)
            .message("User registered successfully")
            .userId(userId)
            .username(username)
            .build();
    }
    
    public static RegisterResponse failure(String message) {
        return RegisterResponse.builder()
            .success(false)
            .message(message)
            .build();
    }
} 