# common-jwt JWT 라이브러리 분석

## 의존성 구성

`common-jwt`는 독립 JAR 형태의 공통 JWT 라이브러리이다. JWT 생성/검증에는 JJWT `0.12.6`을 사용하고, Spring WebFlux, Spring Web, Spring Security, Servlet API, Reactor는 `compileOnly`로 선언되어 있다. 즉 라이브러리 컴파일에는 필요하지만 실제 실행 시점에는 API Gateway나 각 서비스 애플리케이션이 해당 Spring 런타임 의존성을 제공해야 한다.

주요 의존성:

- `io.jsonwebtoken:jjwt-api`
- `io.jsonwebtoken:jjwt-impl`
- `io.jsonwebtoken:jjwt-jackson`
- `org.springframework:spring-webflux`
- `org.springframework:spring-web`
- `org.springframework.security:spring-security-core`
- `org.springframework.security:spring-security-web`
- `jakarta.servlet:jakarta.servlet-api`
- `io.projectreactor:reactor-core`
- `org.slf4j:slf4j-api`
- `org.projectlombok:lombok`

## 토큰 생성 방식

`JwtTokenProvider`는 생성자에서 문자열 secret을 `JwtUtil.generateSecretKey()`로 변환해 `SecretKey`를 만든다. secret 문자열이 Base64이면 Base64 디코딩 결과를 HMAC 키로 사용하고, Base64가 아니면 UTF-8 바이트를 HMAC 키로 사용한다. 키 길이가 부족하면 JJWT의 `WeakKeyException`이 발생한다.

Access Token 생성 시 포함되는 값:

- `sub`: username
- `iat`: issuedAt
- `exp`: expiration
- `tenantId`: tenant의 숫자 PK (`Tenant.id`)
- `tenantSchema`: tenant의 실제 DB 스키마명 (`Tenant.name`). `tenant-provisioning-worker`가
  `CREATE SCHEMA <tenantName>`으로 만든 스키마와 동일한 값이며, `tenantId`(숫자 PK)와는 다른 값이다.
  스키마별 테넌트 라우팅은 이 claim을 기준으로 동작한다.
- `roles`: 로그인 시점의 사용자 역할 목록 (예: `["ROLE_USER", "ROLE_ADMIN"]`)
- 서명: `signWith(actualSecretKey)`

Refresh Token 생성 시 포함되는 값:

- `sub`: username
- `iat`: issuedAt
- `exp`: expiration
- `tenantId`: tenant 식별자
- `type`: `refresh`
- `version`: `1.0`
- 서명: `signWith(actualSecretKey)`

## 토큰 검증 방식

검증의 중심은 `JwtTokenProvider.validateTokenWithResult()`이다. 내부적으로 JJWT parser를 만들고 `verifyWith(actualSecretKey)`로 서명을 검증한 뒤 `parseSignedClaims(token)`을 수행한다.

검증 결과는 boolean만 반환하지 않고 `TokenValidationResult`로 표현된다.

- `VALID`: 서명, 형식, 만료 검증 통과
- `EXPIRED`: `ExpiredJwtException`
- `INVALID`: malformed, signature invalid, unsupported, 기타 예외

Refresh Token은 `validateRefreshToken()`에서 claims를 파싱한 뒤 `type` claim이 `refresh`인지 확인한다.

## 인증 필터 흐름

공통 필터는 두 종류가 있다.

- `ServletJwtAuthenticationFilter`: 일반 Servlet 기반 Spring Boot 서비스용
- `ReactiveJwtAuthenticationFilter`: Spring Cloud Gateway 같은 WebFlux 기반 서비스용

두 필터 모두 `Authorization` 헤더에서 `Bearer ` prefix를 제거해 토큰을 추출한다. 토큰이 없으면 즉시 401을 반환하지 않고 다음 필터로 넘긴다. 공개 경로는 각 서비스의 Spring Security 설정에서 `permitAll()`로 처리할 수 있게 하기 위한 구조이다.

토큰이 있으면 `JwtAuthenticationHandler.validateToken()`을 호출한다. 이 메서드는 토큰 검증 결과와 함께 path, method, clientIp, username, tenantId, tenantSchema, roles를 `JwtAuthenticationContext`에 담는다.

검증에 성공하면 `JwtAuthenticationHandler.createAuthentication()`이 Spring Security `UsernamePasswordAuthenticationToken`을 생성한다. principal은 `JwtUserPrincipal`(username/tenantId/tenantSchema를 담는 `AuthenticatedPrincipal` 구현체, `getName()`은 username을 반환하므로 기존에 `authentication.getName()`을 쓰던 코드는 그대로 동작)이고, 권한은 토큰의 `roles` claim으로부터 구성한 `GrantedAuthority` 목록이다(claim이 비어 있으면 하위 호환을 위해 `ROLE_USER`로 대체).

Servlet 환경에서는 `SecurityContextHolder.getContext().setAuthentication(authentication)`으로 인증 정보를 저장하고, principal이 `JwtUserPrincipal`이면 `com.common.jwt.tenant.TenantContext`(ThreadLocal)에 `tenantSchema`도 함께 세팅한다. 요청 완료 후 `SecurityContextHolder.clearContext()`와 `TenantContext.clear()`로 모두 정리한다. `board-service`는 이 `TenantContext` 값을 Hibernate `CurrentTenantIdentifierResolver`가 읽어 `SET search_path`로 실제 테넌트 스키마를 전환하는 데 사용한다(자세한 내용은 [tenant-schema-routing.md](tenant-schema-routing.md) 참고).

Reactive 환경(`api-gateway`)에서는 `ReactiveSecurityContextHolder.withAuthentication(authentication)`을 Reactor context에 넣는다. `UserContextFilter`는 이 Authentication의 principal이 `JwtUserPrincipal`이면 `X-Tenant-Schema`, `X-User-Roles` 헤더도 다운스트림 서비스로 전달한다(관측/로깅용이며, `board-service` 자체는 JWT를 직접 검증하므로 이 헤더에 의존하지 않는다).

## 블랙리스트 처리

`TokenBlacklistService`는 Redis 같은 외부 저장소 구현체를 붙이기 위한 인터페이스이다. 동기/비동기/CompletableFuture 형태의 API를 제공한다.

검증 실패 토큰은 `JwtAuthenticationHandler.blacklistInvalidToken()`을 통해 24시간 동안 블랙리스트에 추가될 수 있다. 이미 블랙리스트에 있는 토큰은 인증 실패로 처리된다.

주의할 점은 블랙리스트 저장소 조회 중 오류가 발생하면 현재 구현은 서비스 중단 방지를 위해 허용적으로 처리한다.

## 활용 방식

API Gateway에서는 `JwtFilterConfigurer.createReactiveFilter()`로 `ReactiveJwtAuthenticationFilter`를 생성해 Gateway 보안 필터 체인에 등록하면 된다. 이 경우 Gateway 진입점에서 JWT를 검증하고 인증 컨텍스트를 구성할 수 있다.

일반 마이크로서비스에서는 `JwtFilterConfigurer.createServletFilter()`로 `ServletJwtAuthenticationFilter`를 생성해 Spring Security 필터 체인에 추가하면 된다.

로그인/인증 서비스에서는 `JwtTokenProvider.generateAccessToken()`과 `generateRefreshToken()`을 사용해 토큰을 발급한다.

각 서비스의 비즈니스 로직에서는 Spring Security의 현재 `Authentication`을 통해 username(`getName()`)을 얻을 수 있고, tenantId/tenantSchema가 필요하면 principal을 `JwtUserPrincipal`로 캐스팅해서 꺼낸다. 요청 스레드 안에서 현재 테넌트 스키마만 필요하다면 `com.common.jwt.tenant.TenantContext.getCurrentTenant()`를 사용한다.

## 현재 구현상 유의점

- Access Token에는 `type=access` claim이 없다. `validateToken()`은 refresh token도 일반 JWT로는 유효하다고 판단할 수 있다.
- 테스트의 `TEST_EXPIRATION_TIME = 3600`은 주석상 1시간이지만 구현은 millisecond 값으로 더한다. 실제로는 3.6초로 동작한다. 운영 설정에서는 만료시간 단위를 명확히 맞춰야 한다. (`JwtUtilTest#generateAccessToken_ShouldSetCorrectExpirationTime`이 이 단위 불일치 때문에 이미 실패 중이며, 이번 테넌트 스키마 라우팅 작업과는 무관한 기존 결함이다.)

> 2026-09-11 기준: 과거에 이 문서가 지적하던 "role claim을 읽지 않고 항상 ROLE_USER만 부여", "tenantId가 Authentication 객체에 담기지 않음" 문제는 테넌트 스키마 라우팅 작업에서 해결되었다. 자세한 배경과 전체 구조는 [tenant-schema-routing.md](tenant-schema-routing.md) 참고.
