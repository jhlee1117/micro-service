package com.microservices.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest {
    
    @NotBlank
    private String username;
    private String password;
}
