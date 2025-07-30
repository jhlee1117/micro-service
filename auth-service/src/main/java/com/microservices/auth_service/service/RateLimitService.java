package com.microservices.auth_service.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.common.exceptions.TooManyAttemptsException;

@Service
public class RateLimitService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${rate-limit.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${rate-limit.login.window-minutes:15}")
    private int windowMinutes;

    @Value("${rate-limit.login.block-duration-minutes:30}")
    private int blockDurationMinutes;

    private static final String LOGIN_ATTEMPTS_KEY_PREFIX = "login_attempts:";
    private static final String BLOCKED_IP_KEY_PREFIX = "blocked_ip:";
    private static final String ACCESS_TOKEN_BLACKLIST_KEY_PREFIX = "blacklist:";

    /**
     * 로그인 시도 횟수를 체크하고 제한을 적용합니다.
     * 
     * @param clientIp 클라이언트 IP 주소
     * @throws TooManyAttemptsException 허용된 시도 횟수를 초과한 경우
     */
    public void checkLoginAttempts(String clientIp) {
        // 차단된 IP인지 확인
        if (isIpBlocked(clientIp)) {
            throw new TooManyAttemptsException("IP is blocked due to too many failed attempts");
        }

        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + clientIp;
        String blockedKey = BLOCKED_IP_KEY_PREFIX + clientIp;

        // 현재 시도 횟수 가져오기
        Integer currentAttempts = (Integer) redisTemplate.opsForValue().get(attemptsKey);
        
        if (currentAttempts == null) {
            // 첫 번째 시도
            redisTemplate.opsForValue().set(attemptsKey, 1, windowMinutes, TimeUnit.MINUTES);
        } else {
            // 기존 시도 횟수 증가
            int newAttempts = currentAttempts + 1;
            
            if (newAttempts > maxAttempts) {
                // 최대 시도 횟수 초과 - IP 차단
                redisTemplate.opsForValue().set(blockedKey, LocalDateTime.now().toString(), 
                    blockDurationMinutes, TimeUnit.MINUTES);
                redisTemplate.delete(attemptsKey);
                throw new TooManyAttemptsException("Too many login attempts. IP blocked for " + 
                    blockDurationMinutes + " minutes.");
            } else {
                // 시도 횟수 업데이트 (TTL 유지)
                redisTemplate.opsForValue().set(attemptsKey, newAttempts, windowMinutes, TimeUnit.MINUTES);
            }
        }
    }

    /**
     * 성공적인 로그인 후 시도 횟수를 리셋합니다.
     * 
     * @param clientIp 클라이언트 IP 주소
     */
    public void resetLoginAttempts(String clientIp) {
        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + clientIp;
        redisTemplate.delete(attemptsKey);
    }

    /**
     * IP가 차단되었는지 확인합니다.
     * 
     * @param clientIp 클라이언트 IP 주소
     * @return 차단 여부
     */
    protected boolean isIpBlocked(String clientIp) {
        String blockedKey = BLOCKED_IP_KEY_PREFIX + clientIp;
        return redisTemplate.hasKey(blockedKey);
    }

    /**
     * IP의 남은 차단 시간을 반환합니다.
     * 
     * @param clientIp 클라이언트 IP 주소
     * @return 남은 차단 시간 (초), 차단되지 않은 경우 -1
     */
    public long getRemainingBlockTime(String clientIp) {
        String blockedKey = BLOCKED_IP_KEY_PREFIX + clientIp;
        Long ttl = redisTemplate.getExpire(blockedKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : -1;
    }

    /**
     * IP의 현재 로그인 시도 횟수를 반환합니다.
     * 
     * @param clientIp 클라이언트 IP 주소
     * @return 현재 시도 횟수
     */
    public int getCurrentAttempts(String clientIp) {
        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + clientIp;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(attemptsKey);
        return attempts != null ? attempts : 0;
    }

    /**
     * Redis 연결 상태를 확인합니다.
     * 
     * @return Redis 사용 가능 여부
     */
    public boolean isRedisAvailable() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 특정 IP의 차단을 수동으로 해제합니다.
     * 
     * @param clientIp 클라이언트 IP 주소
     */
    public void unblockIp(String clientIp) {
        String blockedKey = BLOCKED_IP_KEY_PREFIX + clientIp;
        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + clientIp;
        
        redisTemplate.delete(blockedKey);
        redisTemplate.delete(attemptsKey);
    }

    /**
     * 모든 Rate Limit 데이터를 초기화합니다.
     */
    public void clearAllRateLimitData() {
        // 주의: 운영 환경에서는 사용하지 마세요
        redisTemplate.delete(redisTemplate.keys(LOGIN_ATTEMPTS_KEY_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(BLOCKED_IP_KEY_PREFIX + "*"));
    }

    public void addAccessTokenToBlacklist(String accessToken) {
        String accessTokenKey = ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + accessToken;
        redisTemplate.opsForValue().set(accessTokenKey, "true", 3600, TimeUnit.SECONDS);
    }
} 