package com.microservices.auth_service.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtValidators;

@Configuration
public class OAuth2ClientConfig {

    @Bean
    public JwtDecoderFactory<ClientRegistration> idTokenDecoderFactory(
        @Value("${app.oauth2.id-token-clock-skew:300s}") Duration idTokenClockSkew
    ) {
        OidcIdTokenDecoderFactory decoderFactory = new OidcIdTokenDecoderFactory();
        decoderFactory.setJwtValidatorFactory(clientRegistration -> idTokenValidator(clientRegistration, idTokenClockSkew));
        return decoderFactory;
    }

    private OAuth2TokenValidator<Jwt> idTokenValidator(
        ClientRegistration clientRegistration,
        Duration idTokenClockSkew
    ) {
        OidcIdTokenValidator oidcIdTokenValidator = new OidcIdTokenValidator(clientRegistration);
        oidcIdTokenValidator.setClockSkew(idTokenClockSkew);
        return JwtValidators.createDefaultWithValidators(oidcIdTokenValidator);
    }
}
