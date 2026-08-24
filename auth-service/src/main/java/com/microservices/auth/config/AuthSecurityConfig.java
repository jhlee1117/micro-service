package com.microservices.auth.config;

import com.common.jwt.JwtTokenProvider;
import com.common.jwt.config.JwtFilterConfigurer;
import com.common.jwt.filter.ServletJwtAuthenticationFilter;
import com.microservices.auth.oauth.OAuth2AuthenticationFailureHandler;
import com.microservices.auth.oauth.OAuth2AuthenticationSuccessHandler;
import com.microservices.auth.security.CustomAccessDeniedHandler;
import com.microservices.auth.security.CustomAuthenticationEntryPoint;
import com.microservices.auth.security.redis.AuthRedisService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Configuration
@EnableWebSecurity
public class AuthSecurityConfig {

  @Autowired private UserDetailsService userDetailsService;

  @Autowired private JwtTokenProvider jwtTokenProvider;

  @Autowired private AuthRedisService authRedisService;

  @Autowired private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

  @Autowired private CustomAccessDeniedHandler customAccessDeniedHandler;

  @Autowired
  private ObjectProvider<OAuth2AuthenticationSuccessHandler> oauth2AuthenticationSuccessHandler;

  @Autowired
  private ObjectProvider<OAuth2AuthenticationFailureHandler> oauth2AuthenticationFailureHandler;

  @Autowired private ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

  @Value("${app.security.public-dev-endpoints:false}")
  private boolean publicDevelopmentEndpoints;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    ServletJwtAuthenticationFilter jwtAuthenticationFilter =
        JwtFilterConfigurer.createServletFilter(jwtTokenProvider, authRedisService);

    HttpSecurity configured =
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(
                auth -> {
                    auth.requestMatchers(
                            "/auth/login",
                            "/auth/register",
                            "/auth/refresh",
                            "/auth/hello",
                            "/auth/oauth/signup/complete",
                            "/oauth2/**",
                            "/login/oauth2/**")
                        .permitAll();

                    if (publicDevelopmentEndpoints) {
                      auth.requestMatchers("/actuator/**", "/h2-console/**")
                          .permitAll()
                          .requestMatchers("/swagger-ui/**", "/swagger-ui.html")
                          .permitAll()
                          .requestMatchers("/v3/api-docs/**", "/swagger-resources/**")
                          .permitAll();
                    }

                    auth.anyRequest().authenticated();
                  })
            .headers(
                headers -> {
                  headers.frameOptions(frame -> frame.sameOrigin());

                  if (publicDevelopmentEndpoints) {
                    headers
                        .addHeaderWriter(
                            new DelegatingRequestMatcherHeaderWriter(
                                new AntPathRequestMatcher("/actuator/**"),
                                new StaticHeadersWriter("X-Frame-Options", "")))
                        .addHeaderWriter(
                            new DelegatingRequestMatcherHeaderWriter(
                                new AntPathRequestMatcher("/h2-console/**"),
                                new StaticHeadersWriter("X-Frame-Options", "")));
                  }
                })
            .exceptionHandling(
                ex ->
                    ex.authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    if (clientRegistrationRepository.getIfAvailable() != null) {
      configured.oauth2Login(
          oauth2 ->
              oauth2
                  .successHandler(oauth2AuthenticationSuccessHandler.getObject())
                  .failureHandler(oauth2AuthenticationFailureHandler.getObject()));
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
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
      throws Exception {
    return authConfig.getAuthenticationManager();
  }
}
