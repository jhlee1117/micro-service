# Agent Workspace Guide

AI Agent는 작업을 시작할 때 이 파일을 먼저 읽고, 필요한 경우 `docs/README.md`와 관련 세부 문서를 이어서 확인합니다.

## Project Shape

이 저장소는 Java 17, Spring Boot 3.x, Spring Cloud, Gradle 멀티 모듈 기반 MSA 프로젝트입니다.

- `api-gateway`: 외부 요청 진입점, 라우팅, WebFlux 기반 JWT 검증
- `auth-service`: 인증, 사용자, 테넌트, RBAC, OAuth, 메뉴/모듈 관리
- `board-service`: 게시판 도메인 예제 서비스
- `tenant-provisioning-worker`: RabbitMQ 이벤트를 수신해 테넌트 DB 스키마 생성
- `common-jwt`: JWT Provider, Servlet/WebFlux 인증 필터, 블랙리스트 인터페이스
- `common-exceptions`: 공통 예외 타입과 에러 코드
- `common-util`: 공통 유틸리티

## Documentation Map

- 아키텍처와 이벤트 흐름: `docs/architecture`
- ArchUnit 아키텍처 검증 기준: `docs/architecture/archunit-rules.md`
- 의사결정 기록: `docs/adr`
- API 명세: `docs/api`
- DB 설계: `docs/database`
- Docker Compose, 로그, 운영: `docs/deployment`
- MCP 설정: `docs/deployment/mcp-configuration.md`
- 테스트 절차: `docs/testing`
- 개발 표준과 설계 원칙: `docs/standards`

## Standards

코드 변경 전 작업 성격에 맞는 표준 문서를 확인합니다.

- 공통 코드 품질 기준: `docs/standards/clean-code.md`
- 설계 패턴, 이벤트, 마이크로서비스 분리 판단: `docs/standards/design-principles.md`
- Java 스타일, 메서드 설계, null 처리, 주석 기준: `docs/standards/java-style.md`
- Spring 계층 책임, DI, 트랜잭션, 설정, 보안 기준: `docs/standards/spring-conventions.md`
- 신규 동작, 버그 수정, 테스트 범위 판단: `docs/standards/testing-tdd.md`

우선순위가 충돌하면 프로젝트 표준 문서, 현재 모듈의 기존 패턴, 일반 프레임워크 예시 순으로 따릅니다.

## Working Rules

- 기존 모듈 경계와 패키지 구조를 우선 유지합니다.
- 새 Java 패키지, 계층, 의존성을 추가할 때는 `docs/architecture/archunit-rules.md`와 각 모듈의 `*ArchitectureTest`를 먼저 확인합니다.
- `docs/standards`의 기준과 기존 코드가 충돌하는 대규모 스타일 변경은 별도 작업으로 분리합니다.
- 공통 JWT, 예외, 유틸 변경은 사용하는 서비스 전체의 영향도를 확인합니다.
- 서비스 간 통신 주소는 로컬과 Docker 프로파일을 구분해서 봅니다.
- Docker 환경에서는 컨테이너 이름 기반 DNS와 `microservices-network` 구성을 확인합니다.
- MCP 도구가 필요한 작업은 `.mcp.json`, `.codex/config.toml`, `.gemini/settings.json`와
  `docs/deployment/mcp-configuration.md`의 서버별 용도를 확인합니다.
- 인증/권한 변경은 Gateway와 개별 서비스의 Security 설정을 함께 검토합니다.
- 테넌트 생성 흐름 변경은 `auth-service`, RabbitMQ 설정, `tenant-provisioning-worker`를 함께 확인합니다.

## Verification

일반 변경:

```bash
./gradlew test
```

스타일 검증:

```bash
./gradlew spotlessCheck checkstyleMain checkstyleTest
```

포맷, 스타일, 아키텍처 검증:

```bash
./gradlew spotlessCheck checkstyleMain checkstyleTest test
```

전체 빌드:

```bash
./gradlew clean build
```

Docker 통합 확인:

```bash
docker-compose up -d --build
docker-compose logs -f
```

## Specialized Agent Notes

인프라 작업을 맡은 에이전트는 `docs/deployment/msa-infra-agent-notes.md`의 Docker Compose, 컨테이너 네트워크, PostgreSQL, Redis, Eureka 관련 지침도 참고합니다.

새 Spring Boot 마이크로서비스 모듈을 추가할 때는 `docs/deployment/add-new-service-guide.md`의 절차와 템플릿을 따릅니다.
