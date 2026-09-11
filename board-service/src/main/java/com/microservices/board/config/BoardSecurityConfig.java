package com.microservices.board.config;

import com.common.jwt.JwtTokenProvider;
import com.common.jwt.config.JwtFilterConfigurer;
import com.common.jwt.filter.ServletJwtAuthenticationFilter;
import com.microservices.board.security.SuperAdminTenantOverrideFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class BoardSecurityConfig {

  @Autowired private JwtTokenProvider jwtTokenProvider;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    ServletJwtAuthenticationFilter jwtAuthenticationFilter =
        JwtFilterConfigurer.createServletFilter(jwtTokenProvider);

    return http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/actuator/**").permitAll().anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(new SuperAdminTenantOverrideFilter(), ServletJwtAuthenticationFilter.class)
        .build();
  }
}
