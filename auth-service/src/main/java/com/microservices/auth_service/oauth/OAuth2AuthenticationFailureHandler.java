package com.microservices.auth_service.oauth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final String frontendUrl;
    private final String failurePath;

    public OAuth2AuthenticationFailureHandler(
        @Value("${frontend.url:http://localhost:3000}") String frontendUrl,
        @Value("${frontend.oauth2.failure-path:/oauth2/error}") String failurePath
    ) {
        this.frontendUrl = frontendUrl;
        this.failurePath = failurePath;
    }

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + failurePath)
            .queryParam("error", "oauth2_login_failed")
            .queryParam("message", exception.getMessage())
            .build()
            .encode()
            .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
