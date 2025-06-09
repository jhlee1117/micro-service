package com.microservices.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;

import com.microservices.api_gateway.security.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
        
        // RedirectServerAuthenticationEntryPoint is used to redirect unauthenticated requests to the login page.
        // In a real-world application, you might want to handle this differently, such as returning a 401 Unauthorized status.
        // However, for the purpose of this example, we will use a redirect to a login page.
        RedirectServerAuthenticationEntryPoint authEntryPoint = 
            new RedirectServerAuthenticationEntryPoint("/auth/login");

	    return http.csrf(csrfCustomizer -> csrfCustomizer.disable())
            .exceptionHandling(exceptionHandlingCustomizer -> 
                exceptionHandlingCustomizer.authenticationEntryPoint(authEntryPoint))
            .authorizeExchange(exchanges -> exchanges.pathMatchers("/auth/login", "/auth/register").permitAll()
            .anyExchange().authenticated()
            )
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build(); 
        
    }
}
