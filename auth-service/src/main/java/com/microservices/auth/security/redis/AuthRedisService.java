package com.microservices.auth.security.redis;

import com.common.jwt.authentication.TokenBlacklistService;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** Auth Service용 Redis 기반 토큰 블랙리스트 서비스 */
@Service
public class AuthRedisService implements TokenBlacklistService {

  private static final Logger logger = LoggerFactory.getLogger(AuthRedisService.class);
  private final RedisTemplate<String, Object> redisTemplate;
  private static final String ACCESS_TOKEN_BLACKLIST_KEY_PREFIX = "blacklist:";

  public AuthRedisService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void blacklistToken(String token, long expireTimeMs) {
    try {
      redisTemplate
          .opsForValue()
          .set(
              ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + token,
              "blacklisted",
              Duration.ofMillis(expireTimeMs));
      logger.debug(
          "토큰이 블랙리스트에 추가되었습니다: {}", token.substring(0, Math.min(10, token.length())) + "...");
    } catch (RedisConnectionFailureException e) {
      logger.error("Redis 연결 실패로 토큰 블랙리스트 추가 실패: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("토큰 블랙리스트 추가 중 오류 발생: {}", e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public boolean isBlacklisted(String token) {
    try {
      return Boolean.TRUE.equals(redisTemplate.hasKey(ACCESS_TOKEN_BLACKLIST_KEY_PREFIX + token));
    } catch (RedisConnectionFailureException e) {
      logger.error("Redis 연결 실패로 블랙리스트 확인 실패: {}", e.getMessage());
      return false; // 연결 실패 시 허용적으로 처리
    } catch (Exception e) {
      logger.error("블랙리스트 확인 중 오류 발생: {}", e.getMessage(), e);
      return false; // 오류 시 허용적으로 처리
    }
  }
}
