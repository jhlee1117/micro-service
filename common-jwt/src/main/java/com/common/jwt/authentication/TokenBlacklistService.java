package com.common.jwt.authentication;

import reactor.core.publisher.Mono;
import java.util.concurrent.CompletableFuture;

/**
 * 토큰 블랙리스트 관리 인터페이스
 * Redis 또는 다른 저장소를 통한 토큰 블랙리스트 관리
 */
public interface TokenBlacklistService {
    
    /**
     * 토큰을 블랙리스트에 추가 (동기식)
     */
    void blacklistToken(String token, long expireTimeMs);
    
    /**
     * 토큰이 블랙리스트에 있는지 확인 (동기식)
     */
    boolean isBlacklisted(String token);
    
    /**
     * 토큰을 블랙리스트에 추가 (비동기식 - Reactive)
     */
    default Mono<Void> blacklistTokenAsync(String token, long expireTimeMs) {
        return Mono.fromRunnable(() -> blacklistToken(token, expireTimeMs));
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인 (비동기식 - Reactive)
     */
    default Mono<Boolean> isBlacklistedAsync(String token) {
        return Mono.fromCallable(() -> isBlacklisted(token));
    }
    
    /**
     * 토큰을 블랙리스트에 추가 (비동기식 - CompletableFuture)
     */
    default CompletableFuture<Void> blacklistTokenFuture(String token, long expireTimeMs) {
        return CompletableFuture.runAsync(() -> blacklistToken(token, expireTimeMs));
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인 (비동기식 - CompletableFuture)
     */
    default CompletableFuture<Boolean> isBlacklistedFuture(String token) {
        return CompletableFuture.supplyAsync(() -> isBlacklisted(token));
    }
}








