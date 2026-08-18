# Redis 기반 Rate Limit 구조 구현

이 문서는 Auth Service에서 구현된 Redis 기반 Rate Limit 구조에 대해 설명합니다.

## 개요

Rate Limit은 로그인 시도 횟수를 제한하여 무차별 대입 공격(Brute Force Attack)을 방지하는 보안 기능입니다. Redis를 사용하여 분산 환경에서도 데이터를 공유할 수 있습니다.

## 구현된 기능

### Redis 기반 Rate Limit
- `RateLimitService` 클래스로 구현
- Redis를 사용하여 데이터 저장 및 공유
- TTL(Time To Live)을 통한 자동 만료 처리
- 분산 환경에서도 일관된 Rate Limit 적용

## 설정

### Redis 설정
`application.yml`에서 Redis 연결 정보를 설정합니다:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
```

### Rate Limit 설정
```yaml
rate-limit:
  login:
    max-attempts: 5        # 최대 허용 시도 횟수
    window-minutes: 15     # 시도 횟수 초기화 윈도우 (분)
    block-duration-minutes: 30  # IP 차단 시간 (분)
```

## 동작 방식

1. **로그인 시도 시**:
   - 클라이언트 IP 주소를 추출
   - Redis에서 해당 IP의 시도 횟수를 확인
   - 최대 시도 횟수 초과 시 IP 차단 (TTL 설정)

2. **성공적인 로그인 시**:
   - 해당 IP의 시도 횟수를 Redis에서 삭제

3. **차단된 IP**:
   - 설정된 시간 동안 로그인 시도 불가
   - Redis TTL에 의해 자동 해제

## API 엔드포인트

### 로그인
```
POST /auth/login
```

### Rate Limit 상태 확인
```
GET /auth/rate-limit/status?ip={IP주소}
```

응답 예시:
```json
{
  "ip": "192.168.1.1",
  "currentAttempts": 3,
  "remainingBlockTime": -1,
  "isBlocked": false,
  "redisAvailable": true
}
```

### Redis 연결 상태 확인
```
GET /auth/rate-limit/redis-status
```

### IP 차단 해제 (관리자용)
```
DELETE /auth/rate-limit/unblock?ip={IP주소}
```

### 모든 Rate Limit 데이터 초기화 (관리자용)
```
DELETE /auth/rate-limit/clear-all
```

## 사용 예시

### 1. 정상적인 로그인
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"password"}'
```

### 2. Rate Limit 상태 확인
```bash
curl "http://localhost:8081/auth/rate-limit/status?ip=192.168.1.1"
```

### 3. Redis 상태 확인
```bash
curl "http://localhost:8081/auth/rate-limit/redis-status"
```

### 4. IP 차단 해제
```bash
curl -X DELETE "http://localhost:8081/auth/rate-limit/unblock?ip=192.168.1.1"
```

## Redis 키 구조

### 로그인 시도 횟수
- 키: `login_attempts:{IP주소}`
- 값: 시도 횟수 (Integer)
- TTL: window-minutes 설정값

### 차단된 IP
- 키: `blocked_ip:{IP주소}`
- 값: 차단 시간 (String)
- TTL: block-duration-minutes 설정값

## 보안 고려사항

1. **IP 스푸핑 방지**: 프록시나 로드 밸런서 환경에서 실제 클라이언트 IP 추출
2. **Redis 보안**: Redis 서버 접근 제한 및 인증 설정
3. **TTL 활용**: Redis의 TTL 기능으로 자동 만료 처리
4. **분산 환경 지원**: 여러 서버에서 동일한 Rate Limit 적용

## 장점

1. **분산 환경 지원**: 여러 서버에서 동일한 Rate Limit 데이터 공유
2. **영구 저장**: 애플리케이션 재시작 시에도 데이터 유지
3. **자동 만료**: Redis TTL로 메모리 효율적 관리
4. **확장성**: Redis 클러스터로 대용량 처리 가능

## 모니터링 및 관리

### Redis 연결 상태 모니터링
```bash
# Redis 상태 확인
curl "http://localhost:8081/auth/rate-limit/redis-status"

# 특정 IP 상태 확인
curl "http://localhost:8081/auth/rate-limit/status?ip=192.168.1.1"
```

### 관리자 기능
- IP 차단 해제
- 전체 Rate Limit 데이터 초기화
- Redis 연결 상태 확인

## 테스트

Rate Limit 기능을 테스트하려면:

1. **Redis 서버 실행 확인**
2. **애플리케이션 실행**
3. **로그인 시도 반복**
4. **Rate Limit 상태 확인**

```bash
# Redis 연결 테스트
redis-cli ping

# 애플리케이션 실행
./gradlew bootRun

# Rate Limit 테스트
for i in {1..6}; do
  curl -X POST http://localhost:8081/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"wrong"}' \
    -w "\nHTTP Status: %{http_code}\n"
done
```

## 주의사항

- Redis 서버가 실행 중이어야 합니다.
- Redis 연결 실패 시 Rate Limit 기능이 동작하지 않을 수 있습니다.
- 운영 환경에서는 Redis 보안 설정을 반드시 적용하세요.
- 관리자 기능은 운영 환경에서 신중하게 사용하세요. 