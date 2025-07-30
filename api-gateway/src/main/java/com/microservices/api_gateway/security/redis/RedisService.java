package com.microservices.api_gateway.security.redis;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException; // Redis 연결 실패 예외
import org.springframework.data.redis.core.RedisTemplate; // Redis 템플릿
import org.springframework.stereotype.Service; // 서비스 어노테이션

import reactor.core.publisher.Mono; // 리액터 Mono

@Service
public class RedisService {

    private static final Logger logger = LoggerFactory.getLogger(RedisService.class); // 로거 생성
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ACCESS_TOKEN_BLACKLIST_KEY_PREFIX = "blacklist:";

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> isBlacklisted(String token) {
        return Mono.fromCallable(() -> {
            try {
                return redisTemplate.hasKey(ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + token); // Redis에서 키 존재 여부 확인
            } catch (RedisConnectionFailureException | QueryTimeoutException e) {
                logger.error("Redis connection error while checking blacklist for token: {}", token, e);
                return false; // Redis 연결 실패 시 false 반환 (서비스 지속성을 위해)
                              // 필요에 따라 사용자 정의 예외를 던질 수도 있음.
                              // throw new CustomRedisUnavailableException("Redis is unavailable", e);
            }
        });
    }

    public Mono<Void> blacklistToken(String token, long expirationTime) {
        return Mono.fromRunnable(() -> {
            try {
                // 키를 일관성 있게 설정
                redisTemplate.opsForValue().set(ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + token, "blacklisted", Duration.ofMillis(expirationTime));
            } catch (RedisConnectionFailureException | QueryTimeoutException e) {
                logger.error("Redis connection error while blacklisting token: {}", token, e);
                throw e;
            }
        });
    }

}
