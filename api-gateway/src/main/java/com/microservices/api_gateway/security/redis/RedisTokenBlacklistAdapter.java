package com.microservices.api_gateway.security.redis;

import com.common.jwt.authentication.TokenBlacklistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Redis 기반 토큰 블랙리스트 서비스 어댑터
 * 기존 RedisService를 공통 TokenBlacklistService 인터페이스에 맞게 래핑
 */
@Component
public class RedisTokenBlacklistAdapter implements TokenBlacklistService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisTokenBlacklistAdapter.class);
    
    private final RedisService redisService;
    
    public RedisTokenBlacklistAdapter(RedisService redisService) {
        this.redisService = redisService;
    }
    
    @Override
    public void blacklistToken(String token, long expireTimeMs) {
        try {
            redisService.blacklistToken(token, expireTimeMs).block();
        } catch (Exception e) {
            logger.error("Redis 토큰 블랙리스트 추가 실패: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isBlacklisted(String token) {
        try {
            return Boolean.TRUE.equals(redisService.isBlacklisted(token).block());
        } catch (Exception e) {
            logger.error("Redis 블랙리스트 확인 실패: {}", e.getMessage(), e);
            return false; // 오류 시 허용적으로 처리
        }
    }
    
    @Override
    public Mono<Void> blacklistTokenAsync(String token, long expireTimeMs) {
        return redisService.blacklistToken(token, expireTimeMs)
            .onErrorResume(error -> {
                logger.error("Redis 비동기 토큰 블랙리스트 추가 실패: {}", error.getMessage(), error);
                return Mono.empty();
            });
    }
    
    @Override
    public Mono<Boolean> isBlacklistedAsync(String token) {
        return redisService.isBlacklisted(token)
            .onErrorReturn(false); // 오류 시 허용적으로 처리
    }
}








