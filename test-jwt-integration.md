# JWT 공통 필터 통합 테스트 가이드

## Zero Trust 원칙 검증

### 1. 서비스 시작
```bash
# Auth Service 시작 (8081)
cd auth-service
./gradlew bootRun

# API Gateway 시작 (8080)
cd api-gateway
./gradlew bootRun
```

### 2. 인증 테스트

#### 2.1 로그인 (JWT 토큰 획득)
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin"
  }'
```

응답에서 `accessToken`을 저장하세요.

#### 2.2 Auth Service 보안 엔드포인트 테스트

**토큰 없이 접근 (401 Unauthorized 예상)**
```bash
curl -X GET http://localhost:8081/secure/user-info
```

**유효한 토큰으로 접근 (200 OK 예상)**
```bash
curl -X GET http://localhost:8081/secure/user-info \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**프로필 정보 조회**
```bash
curl -X GET http://localhost:8081/secure/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 2.3 API Gateway를 통한 접근 테스트

**Gateway를 통한 Auth Service 접근**
```bash
curl -X GET http://localhost:8080/auth-service/secure/user-info \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. 블랙리스트 테스트

#### 3.1 로그아웃 (토큰 블랙리스트 추가)
```bash
curl -X POST http://localhost:8081/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 3.2 블랙리스트된 토큰으로 접근 시도 (401 Unauthorized 예상)
```bash
curl -X GET http://localhost:8081/secure/user-info \
  -H "Authorization: Bearer YOUR_BLACKLISTED_TOKEN"
```

### 4. Zero Trust 검증 항목

✅ **모든 요청에 대한 인증 확인**
- `/secure/*` 경로는 JWT 토큰 없이 접근 불가
- 유효하지 않은 토큰으로 접근 불가

✅ **토큰 검증**
- 만료된 토큰 거부
- 잘못된 서명의 토큰 거부
- 블랙리스트된 토큰 거부

✅ **일관된 보안 정책**
- Auth Service와 API Gateway 모두 동일한 JWT 검증 로직 적용
- 공통 블랙리스트 저장소(Redis) 사용

✅ **로깅 및 모니터링**
- 모든 인증 시도가 로그에 기록됨
- 실패한 인증 시도의 세부 정보 기록

### 5. 예상 로그 출력

**성공한 인증:**
```
JWT 인증 성공: path=/secure/user-info, method=GET, username=admin, clientIp=127.0.0.1
```

**실패한 인증:**
```
JWT 인증 실패: path=/secure/user-info, method=GET, reason=Invalid token, clientIp=127.0.0.1
```

### 6. 추가 보안 기능

- **IP 주소 추적**: 모든 요청의 클라이언트 IP 주소 로깅
- **토큰 자동 블랙리스트**: 유효하지 않은 토큰은 자동으로 블랙리스트에 추가
- **허용적 오류 처리**: Redis 연결 실패 시에도 서비스 중단 방지
