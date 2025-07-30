package com.microservices.api_gateway.security.jwt;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.microservices.api_gateway.security.redis.RedisService;

import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private final JwtTokenValidator jwtTokenValidator;
    private final RedisService redisService;

    @Value("${frontend.url:}")
    private String frontendUrl;

    @Value("${frontend.login-path:}")
    private String loginPath;

    public JwtAuthenticationGatewayFilterFactory(JwtTokenValidator jwtTokenValidator, RedisService redisService) {
        super(Config.class);
        this.jwtTokenValidator = jwtTokenValidator;
        this.redisService = redisService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String token = extractToken(exchange.getRequest());

            // 공개 API 경로 체크 (인증 필요 없는 경로)
            if (isPublicEndpoint(exchange.getRequest().getPath().toString())) {
                return chain.filter(exchange);
            }

            if (token != null && jwtTokenValidator.validateToken(token)) {
                
                return redisService.isBlacklisted(token)
                    .flatMap(isBlackListToken -> {
                        if (Boolean.TRUE.equals(isBlackListToken)) {
                            return handleUnauthenticated(exchange);
                        } else {
                            Authentication auth = jwtTokenValidator.getAuthentication(token);
                            return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                        }
                    });
            }

            // 인증 실패 처리
            return handleUnauthenticated(exchange);
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    private Mono<Void> handleUnauthenticated(ServerWebExchange exchange) {
        // API 요청인지 웹 페이지 요청인지 확인
        String acceptHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.ACCEPT);
        boolean isApiRequest = acceptHeader != null && 
                                (acceptHeader.contains("application/json") || 
                                 acceptHeader.contains("application/xml"));
    
        if (isApiRequest) {
            // API 요청이면 401 반환
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } else {
            String originalUrl = exchange.getRequest().getURI().toString();
            String encodedUrl = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);
            // 웹 요청이면 로그인 페이지로 리다이렉트
            exchange.getResponse().setStatusCode(HttpStatus.FOUND); // 302 Found
            exchange.getResponse().getHeaders().add(HttpHeaders.LOCATION, 
                                         frontendUrl + loginPath + "?redirect=" + encodedUrl);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/login") || 
               path.startsWith("/auth/register") || 
               path.startsWith("/auth/hello") || 
               path.startsWith("/public");
    }

    public static class Config {
        // 필요한 설정이 있다면 여기에 추가
    }
}
