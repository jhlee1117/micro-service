package com.microservices.auth_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class AuthSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable()) // CORS ��Ȱ��ȭ
                .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT ���� ������
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**").permitAll() // 모든 auth 경로 허용
                    .requestMatchers("/actuator/**", "/h2-console/**").permitAll() // Health check 등
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger UI
                    .requestMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll() // Swagger API docs
                    .anyRequest().authenticated() // 나머지는 인증 필요
                )
                .headers(headers -> headers
                    // ⺻������ X-Frame-Options ����
                    .frameOptions(frame -> frame.sameOrigin())
                    // Ư�� ��ο� ���ؼ��� X-Frame-Options ����
                    .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                        new AntPathRequestMatcher("/actuator/**"),
                        new StaticHeadersWriter("X-Frame-Options", "")
                    ))
                    .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                        new AntPathRequestMatcher("/h2-console/**"),
                        new StaticHeadersWriter("X-Frame-Options", "")
                    ))
                )
                .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .build();
    }

    // public static void main(String[] args) {
    //     System.out.println(new BCryptPasswordEncoder().encode("admin"));
    //     System.out.println(new BCryptPasswordEncoder().encode("testuser"));
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
