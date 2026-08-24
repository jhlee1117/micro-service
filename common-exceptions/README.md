# common-exceptions

프로젝트 전반에서 재사용하는 예외 타입, 에러 코드, 에러 응답 모델을 정의하는 공통 Gradle 모듈입니다.

각 서비스는 이 모듈의 `BusinessException`과 도메인별 `ErrorCode`를 사용해 실패 원인을 표현하고, 실제 HTTP 응답 변환은 서비스별 전역 예외 핸들러에서 처리합니다.

## 1. 모듈 책임

- 서비스 공통 예외 타입 제공
- 클라이언트 i18n 처리를 위한 에러 키 제공
- 서버 로그와 기본 응답에 사용할 메시지 제공
- HTTP 상태 코드 매핑 정보 제공
- 표준 에러 응답 DTO 제공

이 모듈은 공통 라이브러리이므로 Spring 또는 특정 애플리케이션 모듈(`auth-service`, `board-service` 등)에 의존하지 않습니다.

## 2. 패키지 구성

```text
com.common.exceptions
├── BusinessException
├── NoTenantException
├── TooManyAttemptsException
└── code
    ├── ErrorCode
    ├── UserErrorCode
    ├── TenantErrorCode
    ├── RoleErrorCode
    ├── PermissionErrorCode
    ├── ModuleErrorCode
    ├── MenuErrorCode
    └── BoardErrorCode

com.common.response
└── ErrorResponse
```

## 3. 핵심 타입

### `ErrorCode`

모든 도메인별 에러 코드 enum이 구현하는 공통 인터페이스입니다.

- `key`: 프런트엔드 i18n 메시지 매핑 키
- `message`: 서버 디버깅, 로그, 기본 응답에 사용할 메시지
- `status`: HTTP 상태 코드

예시:

```java
USER_NOT_FOUND("system.userManagement.userNotFound", "사용자를 찾을 수 없습니다.", 404)
```

### `BusinessException`

비즈니스 규칙 위반을 표현하는 공통 런타임 예외입니다.

- 단순 에러: `new BusinessException(UserErrorCode.USER_NOT_FOUND)`
- 동적 인자가 필요한 에러: `new BusinessException(errorCode, Map.of("userId", userId))`

`BusinessException`은 `ErrorCode`와 `args`를 함께 보관합니다. `args`는 프런트엔드 다국어 메시지 치환값이나 서버 로그 컨텍스트로 사용할 수 있습니다.

### `ErrorResponse`

전역 예외 핸들러에서 클라이언트로 내려보낼 표준 에러 응답 모델입니다.

- `errorCode`: `ErrorCode.getKey()` 값
- `message`: `ErrorCode.getMessage()` 값
- `args`: 메시지 치환 또는 디버깅용 동적 인자
- `timestamp`: 응답 생성 시각
- `traceId`: 필요 시 서버 로그 추적용 ID

## 4. 도메인별 에러 코드

현재 도메인별 에러 코드는 `com.common.exceptions.code` 패키지의 enum으로 관리합니다.

- `UserErrorCode`: 사용자, OAuth 가입 토큰 관련 에러
- `TenantErrorCode`: 테넌트 중복, 미존재 에러
- `RoleErrorCode`: 역할 중복, 미존재, 삭제 제한 에러
- `PermissionErrorCode`: 권한 중복, 미존재 에러
- `ModuleErrorCode`: 모듈 중복, 미존재 에러
- `MenuErrorCode`: 메뉴 중복, 미존재 에러
- `BoardErrorCode`: 게시글 미존재, 권한 없음 에러

## 5. 예외 처리 흐름

일반적인 처리 흐름은 다음과 같습니다.

1. 서비스 계층에서 비즈니스 조건을 검증합니다.
2. 실패 시 도메인별 `ErrorCode`를 담아 `BusinessException`을 던집니다.
3. 각 서비스의 전역 예외 핸들러가 `BusinessException`을 잡습니다.
4. 핸들러가 `ErrorCode`의 `key`, `message`, `status`와 `args`를 사용해 `ErrorResponse`를 생성합니다.
5. 클라이언트는 `errorCode`를 기준으로 다국어 메시지를 매핑합니다.

## 6. 새 에러 코드 추가 기준

새 예외 상황을 추가할 때는 별도 예외 클래스를 먼저 만들기보다 기존 도메인별 `ErrorCode` enum에 상수를 추가하는 방식을 우선합니다.

```java
INVALID_ROLE_NAME("system.roleManagement.invalidRoleName", "역할 이름이 올바르지 않습니다.", 400)
```

추가 시 확인할 내용:

- 같은 도메인의 enum에 배치했는지 확인합니다.
- `key`는 프런트엔드 i18n 키로 사용할 수 있게 안정적인 이름으로 정합니다.
- `message`는 로그와 기본 응답만 보고도 원인을 이해할 수 있게 작성합니다.
- `status`는 HTTP 의미에 맞게 선택합니다.
- 동적 값이 필요하면 `BusinessException(errorCode, args)`를 사용합니다.

## 7. 설계 주의사항

- 이 모듈에는 `@ControllerAdvice`, `ResponseEntity`, Spring Security 등 Spring 의존 코드를 두지 않습니다.
- 특정 서비스의 도메인 객체, DTO, Repository, Service를 참조하지 않습니다.
- 예외 응답 형태를 바꿀 때는 이 모듈을 사용하는 모든 서비스의 전역 예외 핸들러와 API 문서를 함께 확인합니다.
- 공통 모듈 구조를 바꾸는 경우 `CommonExceptionsArchitectureTest`와 `docs/architecture/archunit-rules.md` 기준을 함께 확인합니다.

## 8. 검증

이 모듈의 구조와 의존성은 ArchUnit 테스트로 검증합니다.

```bash
./gradlew test
```
