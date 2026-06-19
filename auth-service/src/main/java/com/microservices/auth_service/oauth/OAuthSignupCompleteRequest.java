package com.microservices.auth_service.oauth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OAuthSignupCompleteRequest(
    @NotBlank
    String signupToken,

    @NotBlank
    @Size(min = 3, max = 100)
    String username,

    @NotBlank
    @Size(max = 100)
    String name,

    @NotNull
    Long tenantId) {

}
