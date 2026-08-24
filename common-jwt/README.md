# common-jwt

프로젝트 전반에서 JWT 생성, 검증, 인증 컨텍스트 변환, Servlet/WebFlux 인증 필터 생성을 공통으로 제공하는 Gradle 모듈입니다.

`auth-service`는 토큰 발급과 검증에 이 모듈을 사용하고, `api-gateway`는 외부 요청의 JWT 인증 처리에 이 모듈의 WebFlux 필터를 사용할 수 있습니다. Servlet 기반 서비스는 Servlet 필터를 통해 같은 인증 로직을 재사용할 수 있습니다.

## 1. 모듈 책임

- JWT access token, refresh token 생성
- JWT 서명, 만료, 형식 검증
- 토큰 claim에서 사용자명과 테넌트 ID 추출
- 검증 결과를 `VALID`, `EXPIRED`, `INVALID` 상태로 표현
- JWT 기반 Spring Security `Authentication` 객체 생성
- Servlet 환경과 Reactive(WebFlux) 환경용 인증 필터 제공
- 토큰 블랙리스트 저장소 연동을 위한 인터페이스 제공

이 모듈은 공통 라이브러리이므로 `auth-service`, `api-gateway`, `board-service` 같은 애플리케이션 모듈에는 의존하지 않습니다. Spring 관련 타입은 필터와 인증 객체 생성을 위해 `compileOnly` 의존성으로만 사용합니다.

## 2. 패키지 구성

```text
com.common.jwt
├── JwtTokenProvider
├── JwtUtil
├── TokenValidationResult
├── authentication
│   ├── JwtAuthenticationContext
│   ├── JwtAuthenticationHandler
│   └── TokenBlacklistService
├── config
│   └── JwtFilterConfigurer
└── filter
    ├── ReactiveJwtAuthenticationFilter
    └── ServletJwtAuthenticationFilter
```

## 3. 전체 인증 흐름

일반적인 요청 인증 흐름은 다음과 같습니다.

1. 클라이언트가 `Authorization: Bearer {token}` 헤더로 요청합니다.
2. Servlet 또는 Reactive 필터가 Authorization 헤더에서 토큰을 추출합니다.
3. `JwtAuthenticationHandler`가 `JwtTokenProvider`를 통해 토큰을 검증합니다.
4. 검증 성공 시 토큰에서 `username`과 `tenantId`를 추출해 `JwtAuthenticationContext`를 만듭니다.
5. 블랙리스트 서비스가 있으면 토큰 차단 여부를 확인합니다.
6. `JwtAuthenticationHandler`가 Spring Security `Authentication` 객체를 생성합니다.
7. 필터가 SecurityContext에 인증 정보를 설정하고 다음 필터 체인으로 진행합니다.

## 4. 토큰 생성과 검증

### `JwtUtil`

JWT를 실제로 생성하는 정적 유틸리티 클래스입니다.

- `generateSecretKey`: 설정 문자열을 `SecretKey`로 변환합니다. Base64 디코딩을 먼저 시도하고, 실패하면 원본 문자열의 UTF-8 바이트를 사용합니다.
- `generateAccessToken`: 사용자명, 테넌트 ID, 비밀키, 만료 시간을 바탕으로 access token을 생성합니다.
- `generateRefreshToken`: 사용자명, 테넌트 ID, 비밀키, 만료 시간을 바탕으로 refresh token을 생성합니다.

access token claim:

- `sub`: 사용자명
- `tenantId`: 테넌트 ID
- `iat`: 발급 시각
- `exp`: 만료 시각

refresh token 추가 claim:

- `type`: `refresh`
- `version`: `1.0`

### `JwtTokenProvider`

서비스가 직접 사용하는 JWT 중심 클래스입니다.

- 생성자에서 secret key 문자열과 access/refresh token 만료 시간을 받습니다.
- `generateAccessToken`, `generateRefreshToken`으로 토큰을 발급합니다.
- `getClaims`, `getUsername`, `getTenantId`로 토큰 정보를 조회합니다.
- `validateTokenWithResult`로 토큰 검증 결과를 상세 상태로 반환합니다.
- `validateRefreshToken`은 refresh token의 `type` claim이 `refresh`인지 확인합니다.

### `TokenValidationResult`

토큰 검증 결과를 표현하는 값 객체입니다.

- `VALID`: 정상 토큰
- `EXPIRED`: 만료된 토큰
- `INVALID`: 서명 오류, 형식 오류, 지원하지 않는 토큰 등

단순히 `true` 또는 `false`만 반환하지 않고 만료와 잘못된 토큰을 구분하므로, 호출 측에서 재발급 흐름이나 인증 실패 응답을 다르게 처리할 수 있습니다.

## 5. 인증 컨텍스트와 Authentication 생성

### `JwtAuthenticationContext`

필터와 핸들러 사이에서 인증 처리에 필요한 정보를 전달하는 컨텍스트 객체입니다.

- token
- path
- method
- clientIp
- validationResult
- username
- tenantId

`isTokenValid`, `isTokenExpired`, `isTokenInvalid` 메서드로 검증 상태를 간단히 확인할 수 있습니다.

### `JwtAuthenticationHandler`

JWT 인증 공통 로직을 담당합니다.

- Authorization 헤더에서 Bearer 토큰 추출
- `JwtTokenProvider`를 통한 토큰 검증
- 검증된 토큰에서 사용자명과 테넌트 ID 추출
- 블랙리스트 확인
- 유효하지 않은 토큰의 블랙리스트 등록
- Spring Security `Authentication` 객체 생성
- 인증 성공/실패 로그 기록

현재 `createAuthentication`은 기본 권한으로 `ROLE_USER`를 부여합니다. 권한 정보를 토큰 claim이나 별도 저장소에서 읽도록 확장하려면 이 메서드의 역할과 호출 서비스를 함께 검토해야 합니다.

## 6. Servlet 필터와 Reactive 필터

### `ServletJwtAuthenticationFilter`

Spring MVC 같은 Servlet 기반 애플리케이션에서 사용하는 `OncePerRequestFilter` 구현체입니다.

- 토큰이 없으면 다음 필터로 진행합니다.
- 토큰이 유효하지 않으면 `401 Unauthorized` JSON 응답을 직접 작성합니다.
- 인증 성공 시 `SecurityContextHolder`에 `Authentication`을 저장합니다.
- 요청 처리가 끝나면 `SecurityContextHolder.clearContext()`로 인증 정보를 정리합니다.

### `ReactiveJwtAuthenticationFilter`

Spring Cloud Gateway 같은 WebFlux 기반 애플리케이션에서 사용하는 `WebFilter` 구현체입니다.

- 토큰이 없으면 다음 필터로 진행합니다.
- 토큰이 유효하지 않으면 `401 Unauthorized` 상태만 설정하고 응답을 완료합니다.
- 블랙리스트 확인은 `TokenBlacklistService.isBlacklistedAsync`를 사용합니다.
- 인증 성공 시 `ReactiveSecurityContextHolder.withAuthentication(authentication)`으로 인증 정보를 전달합니다.

## 7. 블랙리스트 확장 방식

### `TokenBlacklistService`

토큰 블랙리스트 저장소를 추상화한 인터페이스입니다. 공통 모듈은 저장소 구현을 직접 알지 않고, Redis 같은 실제 저장소 구현은 사용하는 서비스에서 제공합니다.

- `blacklistToken`: 토큰을 지정된 시간 동안 블랙리스트에 등록
- `isBlacklisted`: 토큰이 블랙리스트에 있는지 확인
- `blacklistTokenAsync`, `isBlacklistedAsync`: Reactive 환경용 기본 메서드
- `blacklistTokenFuture`, `isBlacklistedFuture`: `CompletableFuture` 기반 기본 메서드

블랙리스트 저장소 오류가 발생하면 현재 인증 핸들러와 Reactive 필터는 서비스 중단을 피하기 위해 허용적으로 처리합니다. 보안 정책을 더 엄격하게 가져가려면 이 동작을 변경할 때 영향도를 함께 검토해야 합니다.

## 8. 필터 생성 유틸리티

### `JwtFilterConfigurer`

서비스 설정 코드에서 필터와 인증 핸들러를 쉽게 생성하도록 돕는 팩토리 성격의 유틸리티 클래스입니다.

- `createReactiveFilter`
- `createServletFilter`
- `createAuthenticationHandler`

각 메서드는 블랙리스트 서비스를 포함하는 버전과 포함하지 않는 버전을 제공합니다.

## 9. 설계 주의사항

- `com.common.jwt`는 `com.microservices..` 하위 애플리케이션 모듈에 의존하지 않습니다.
- `filter` 패키지는 `config` 패키지의 팩토리 클래스에 의존하지 않습니다.
- `authentication` 패키지의 핵심 인증 로직은 `filter`, `config` 패키지에 의존하지 않습니다.
- JWT claim 구조를 바꿀 때는 토큰 발급 서비스, 게이트웨이 인증 필터, refresh token 검증 흐름을 함께 확인합니다.
- `JwtTokenProvider`의 secret key와 만료 시간 설정은 사용하는 서비스의 설정 파일과 함께 확인합니다.
- 필터의 미인증 응답 형식을 바꿀 때는 각 서비스의 Spring Security 예외 처리 방식과 API 응답 규칙을 함께 확인합니다.

## 10. 검증

이 모듈의 구조와 의존성은 ArchUnit 테스트로 검증합니다.

```bash
./gradlew test
```
