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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.common.jwt.JwtTokenProvider;
import com.common.jwt.config.JwtFilterConfigurer;
import com.common.jwt.filter.ServletJwtAuthenticationFilter;
import com.microservices.auth_service.security.redis.AuthRedisService;

@Configuration
@EnableWebSecurity
public class AuthSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuthRedisService authRedisService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        // 공통 JWT 필터 생성
        ServletJwtAuthenticationFilter jwtAuthenticationFilter = 
            JwtFilterConfigurer.createServletFilter(jwtTokenProvider, authRedisService);
        
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable()) // CORS 비활성화
                .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 기반 무상태
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/login", "/auth/register", "/auth/refresh", "/auth/hello").permitAll() // 인증 관련 경로 허용
                    .requestMatchers("/actuator/**", "/h2-console/**").permitAll() // Health check 등
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger UI
                    .requestMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll() // Swagger API docs
                    .anyRequest().authenticated() // 나머지는 인증 필요 (Zero Trust)
                )
                .headers(headers -> headers
                    // 기본으로는 X-Frame-Options 설정
                    .frameOptions(frame -> frame.sameOrigin())
                    // 특정 경로에 대해서는 X-Frame-Options 해제
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
                // JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

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