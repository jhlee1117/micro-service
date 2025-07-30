package com.microservices.auth_service.service;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_REFRESH_TOKEN_KEY = "user_refresh_token:";

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);


    /**
     * Refresh Token 저장
     * @param username
     * @param refreshToken
     * @param expirationTime
     */
    public void saveRefreshToken(String username, String refreshToken, long expirationTime) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userKey = USER_REFRESH_TOKEN_KEY + username;

        redisTemplate.opsForValue().set(tokenKey, username, expirationTime, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(userKey, refreshToken, expirationTime, TimeUnit.MILLISECONDS);
    }


    /**
     * Refresh Token 유효성 검사
     * @param refreshToken
     * @return
     */
    public boolean validateRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        return redisTemplate.hasKey(tokenKey);
    }

    /**
     * Refresh Token 에 해당하는 사용자 이름 조회
     * @param refreshToken
     * @return
     */
    public String getUsernameFromRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        return redisTemplate.opsForValue().get(tokenKey);
    }

    /**
     * 사용자 이름으로 Refresh Token 무효화
     * @param username
     */
    public void invalidateRefreshTokenByUsername(String username) {
        String userKey = USER_REFRESH_TOKEN_KEY + username;
        String refreshToken = redisTemplate.opsForValue().get(userKey);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            redisTemplate.delete(tokenKey);
        }
        redisTemplate.delete(userKey);
    }

    /**
     * Refresh Token 무효화
     * @param refreshToken
     */
    public void invalidateRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String username = redisTemplate.opsForValue().get(tokenKey);

        if (username != null && !username.isEmpty()) {
            String userKey = USER_REFRESH_TOKEN_KEY + username;
            redisTemplate.delete(userKey);
            redisTemplate.delete(tokenKey);
        }
    }

    /**
     * Refresh Token 갱신
     * @param username
     * @param refreshToken
     * @param newRefreshToken
     * @param expirationTime
     */
    public void rotateRefreshToken(String username, String refreshToken, String newRefreshToken, long expirationTime) {
        invalidateRefreshToken(refreshToken); // 기존 Refresh Token 무효화
        saveRefreshToken(username, newRefreshToken, expirationTime); // 새로운 Refresh Token 저장
    }
}
