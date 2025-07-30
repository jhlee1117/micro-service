package com.microservices.auth_service.dto;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {
    
    private String username;
    private String password;
    private String email;
    // private String name;
    private String firstName;
    private String lastName;
    private boolean terms;
    
}
