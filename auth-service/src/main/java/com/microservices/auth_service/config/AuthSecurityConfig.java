package com.microservices.auth_service.config;

import org.springframework.beans.factory.annotation.Autowired;
// ObjectProvider => 빈을 “지금 당장 주입받아 생성하라”가 아니라 “필요한 시점에 조회하겠다”로 바꾸는 도구
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.common.jwt.JwtTokenProvider;
import com.common.jwt.config.JwtFilterConfigurer;
import com.common.jwt.filter.ServletJwtAuthenticationFilter;
import com.microservices.auth_service.oauth.OAuth2AuthenticationFailureHandler;
import com.microservices.auth_service.oauth.OAuth2AuthenticationSuccessHandler;
import com.microservices.auth_service.security.CustomAccessDeniedHandler;
import com.microservices.auth_service.security.CustomAuthenticationEntryPoint;
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

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private ObjectProvider<OAuth2AuthenticationSuccessHandler> oauth2AuthenticationSuccessHandler;

    @Autowired
    private ObjectProvider<OAuth2AuthenticationFailureHandler> oauth2AuthenticationFailureHandler;

    @Autowired
    private ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        ServletJwtAuthenticationFilter jwtAuthenticationFilter =
            JwtFilterConfigurer.createServletFilter(jwtTokenProvider, authRedisService);

        HttpSecurity configured = http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                        "/auth/login",
                        "/auth/register",
                        "/auth/refresh",
                        "/auth/hello",
                        "/oauth2/**",
                        "/login/oauth2/**"
                    ).permitAll()
                    .requestMatchers("/actuator/**", "/h2-console/**").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll()
                    .anyRequest().authenticated()
                )
                .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin())
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
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        if (clientRegistrationRepository.getIfAvailable() != null) {
            configured.oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2AuthenticationSuccessHandler.getObject())
                .failureHandler(oauth2AuthenticationFailureHandler.getObject())
            );
        }

        return configured.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
