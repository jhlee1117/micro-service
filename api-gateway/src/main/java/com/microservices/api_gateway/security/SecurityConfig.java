package com.microservices.api_gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        
        return http.csrf(csrfCustomizer -> csrfCustomizer.disable())
            .cors(corsCustomizer -> corsCustomizer.disable()) // CORS 비활성화 (필요시 별도 설정)
            .exceptionHandling(exceptionHandlingCustomizer -> 
                exceptionHandlingCustomizer.authenticationEntryPoint((exchange, ex) -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/auth/login", "/auth/register", "/auth/hello", "/auth/logout").permitAll()
                .anyExchange().authenticated()
            )
            .build(); 
        
    }
}
