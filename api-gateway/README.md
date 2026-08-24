# api-gateway

MSA 환경에서 외부 요청의 단일 진입점 역할을 하는 Spring Cloud Gateway 모듈입니다.

클라이언트는 개별 서비스 주소로 직접 접근하지 않고 API Gateway로 요청합니다. Gateway는 요청 경로를 기준으로 대상 서비스를 찾고, 필요한 요청은 JWT 인증을 거친 뒤 Eureka에 등록된 실제 서비스 인스턴스로 전달합니다.

## 1. 모듈 책임

- 외부 요청의 단일 진입점 제공
- `auth-service`, `board-service` 등 내부 서비스로 라우팅
- Eureka 기반 서비스 디스커버리와 로드밸런싱 연동
- WebFlux 기반 Spring Security 설정
- `common-jwt`의 Reactive JWT 인증 필터 적용
- Redis 기반 JWT 블랙리스트 확인
- 인증된 사용자 정보를 하위 서비스 요청 헤더로 전달
- local/docker 환경별 Config Server, Eureka, Redis 접속 설정 분리

## 2. 요청 처리 흐름

일반적인 API 호출 흐름은 다음과 같습니다.

```text
Client
  -> API Gateway
  -> JWT 인증 및 블랙리스트 확인
  -> Eureka에서 대상 서비스 인스턴스 조회
  -> 내부 서비스로 요청 전달
```

예시:

```text
GET http://localhost:8070/user/me
Authorization: Bearer {accessToken}
```

처리 순서:

1. 클라이언트가 실제 서비스 주소가 아니라 Gateway 주소(`localhost:8070`)로 요청합니다.
2. `SecurityConfig`의 WebFlux Security 체인이 요청을 가로챕니다.
3. 공개 경로가 아니면 `common-jwt`의 `ReactiveJwtAuthenticationFilter`가 JWT를 검증합니다.
4. Redis 블랙리스트에 등록된 토큰인지 확인합니다.
5. 인증에 성공하면 SecurityContext에 인증 정보를 저장합니다.
6. `UserContextFilter`가 인증된 사용자명을 `X-User-Id` 헤더로 추가합니다.
7. `application.yml`의 route 설정에 따라 대상 서비스를 결정합니다.
8. `lb://service-name` URI를 기준으로 Eureka에서 실제 인스턴스를 찾아 요청을 전달합니다.

## 3. 패키지 구성

```text
com.microservices.gateway
├── ApiGatewayApplication
├── config
│   └── JwtConfig
└── security
    ├── SecurityConfig
    ├── jwt
    │   └── UserContextFilter
    └── redis
        ├── RedisConfig
        ├── RedisService
        └── RedisTokenBlacklistAdapter
```

## 4. 라우팅 구조

Gateway 라우팅은 `application.yml`의 `spring.cloud.gateway.routes`에서 관리합니다.

현재 주요 라우트:

- `auth-service`
  - 대상 URI: `lb://auth-service`
  - 경로: `/auth/**`, `/user/**`, `/secure/**`, `/role/**`, `/tenant/**`, `/oauth2/**`, `/login/oauth2/**`, `/menus/**`, `/module/**`, `/permission/**`
- `board-service`
  - 대상 URI: `lb://board-service`
  - 경로: `/board/**`

`lb://` 접두사는 Spring Cloud LoadBalancer를 사용한다는 의미입니다. Gateway는 서비스 이름으로 Eureka에서 인스턴스를 조회한 뒤, 실제 호스트와 포트로 요청을 전달합니다.

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://auth-service
          predicates:
            - Path=/auth/**, /user/**, /secure/**, /role/**, /tenant/**
```

## 5. 보안 설정

### `SecurityConfig`

WebFlux Security의 핵심 설정 클래스입니다.

- `@EnableWebFluxSecurity`로 Reactive Security 활성화
- CSRF 비활성화
- CORS 비활성화
- 인증 실패 시 `401 Unauthorized` 반환
- 공개 API는 `permitAll`
- 나머지 요청은 JWT 인증 필요
- `ReactiveJwtAuthenticationFilter`를 `AUTHENTICATION` 순서에 추가

현재 인증 없이 접근 가능한 경로:

- `/auth/login`
- `/auth/register`
- `/auth/hello`
- `/auth/refresh`
- `/auth/oauth/signup/complete`
- `/oauth2/**`
- `/login/oauth2/**`
- `/public/**`
- `/tenant/list`

그 외 요청은 기본적으로 인증이 필요합니다.

## 6. JWT 설정

### `JwtConfig`

Gateway에서 사용할 `JwtTokenProvider` Bean을 생성합니다.

- `jwt.secret`
- `jwt.access-token-expire-time`
- `jwt.refresh-token-expire-time`

위 설정값을 읽어 `common-jwt`의 `JwtTokenProvider`를 생성합니다. Gateway와 토큰 발급 서비스는 같은 secret key와 토큰 만료 정책을 공유해야 합니다. 설정이 다르면 Gateway에서 정상 발급된 토큰도 서명 검증에 실패합니다.

`@PostConstruct`의 시간 로그는 토큰 만료 문제를 추적하기 위한 보조 로그입니다. 서버 간 시간이 크게 어긋나면 토큰이 예상보다 빨리 만료되거나 아직 유효하지 않은 것처럼 보일 수 있습니다.

## 7. JWT 인증 필터 연동

Gateway는 JWT 검증 로직을 직접 구현하지 않고 `common-jwt` 모듈을 사용합니다.

```text
SecurityConfig
  -> JwtFilterConfigurer.createReactiveFilter(...)
  -> ReactiveJwtAuthenticationFilter
  -> JwtAuthenticationHandler
  -> JwtTokenProvider
```

이 구조 덕분에 Servlet 기반 서비스와 WebFlux 기반 Gateway가 같은 토큰 생성/검증 규칙을 공유할 수 있습니다.

## 8. Redis 블랙리스트

### `RedisService`

Redis에 블랙리스트 토큰을 저장하고 조회합니다.

- 저장 key prefix: `blacklist:`
- 저장 값: `blacklisted`
- TTL: 호출 시 전달받은 만료 시간

Redis 연결 실패 또는 조회 타임아웃이 발생하면 현재 구현은 `false`를 반환해 요청 처리를 계속할 수 있게 합니다.

### `RedisTokenBlacklistAdapter`

`common-jwt`의 `TokenBlacklistService` 인터페이스를 Gateway의 `RedisService`에 맞게 연결하는 어댑터입니다.

- 동기 메서드: `blacklistToken`, `isBlacklisted`
- Reactive 메서드: `blacklistTokenAsync`, `isBlacklistedAsync`

Gateway의 Reactive JWT 필터는 `isBlacklistedAsync`를 통해 토큰 차단 여부를 확인합니다.

### `RedisConfig`

Redis 접속과 직렬화 설정을 담당합니다.

- `LettuceConnectionFactory`
- connection pool 설정
- `RedisTemplate<String, Object>`
- key는 문자열 직렬화
- value/hash value는 Jackson 기반 직렬화

## 9. 사용자 컨텍스트 전달

### `UserContextFilter`

JWT 인증이 완료된 뒤 SecurityContext에서 인증 정보를 읽어 하위 서비스 요청에 사용자 식별자를 전달하는 GlobalFilter입니다.

```text
Authentication.getName()
  -> X-User-Id header
  -> downstream service
```

하위 서비스는 Gateway가 추가한 `X-User-Id` 헤더를 통해 현재 요청 사용자를 식별할 수 있습니다. 단, 이 헤더는 외부 클라이언트가 직접 신뢰할 수 있는 값이 아니라 Gateway 인증 이후 내부 전달용 값으로 다뤄야 합니다.

## 10. 환경별 설정

### local

`application-local.yml`은 로컬 개발 환경을 기준으로 합니다.

- Config Server: `http://localhost:8888`
- Redis: `localhost:6379`
- Eureka: `http://localhost:8761/eureka/`

### docker

`application-docker.yml`은 Docker Compose 네트워크를 기준으로 합니다.

- Config Server: `http://config-server:8888`
- Redis: `${REDIS_HOST:redis}`
- Eureka: `${EUREKA_URI:http://service-discovery:8761/eureka/}`

Docker 환경에서는 컨테이너 이름 기반 DNS를 사용하므로 localhost 대신 서비스명을 사용합니다.

## 11. 설계 주의사항

- Gateway 코드는 `com.microservices.gateway` 하위 패키지에 둡니다.
- 설정 클래스는 `config` 또는 `security` 패키지에 둡니다.
- `security` 패키지의 컴포넌트는 Gateway `config` 패키지에 의존하지 않습니다.
- Gateway는 라우팅과 인증의 경계 역할을 담당하고, 각 서비스의 비즈니스 로직을 직접 처리하지 않습니다.
- 공개 경로를 추가할 때는 `SecurityConfig`와 Gateway route 설정을 함께 확인합니다.
- JWT claim 구조를 바꿀 때는 `common-jwt`, `auth-service`, `api-gateway`를 함께 확인합니다.
- Redis 장애 시 허용적으로 처리하는 현재 정책은 보안 요구사항에 따라 재검토할 수 있습니다.

## 12. 검증

아키텍처 규칙과 기본 테스트는 Gradle test로 확인합니다.

```bash
./gradlew test
```

스타일과 전체 검증을 함께 수행하려면 다음 명령을 사용합니다.

```bash
./gradlew spotlessCheck checkstyleMain checkstyleTest test
```
