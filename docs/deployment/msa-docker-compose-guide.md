# MSA Docker Compose 통합 가이드 및 작업 지시서

**문서 작성일:** 2026-02-25

---

## 1. 개요 및 목적
현재 독립적으로 개발 중인 Spring Cloud 기반의 마이크로서비스(API Gateway, Service Discovery, Auth Service)와 인프라(Redis, DB)를 하나의 **Docker Compose 환경으로 통합**합니다. 

본 통합 작업의 주요 목적은 다음과 같습니다.
1.  **로컬 개발 환경의 일관성 확보:** 새로운 개발자 온보딩 시 복잡한 세팅 과정 없이 `docker-compose up -d` 명령어 하나로 전체 서비스 인프라를 구동할 수 있도록 합니다. ("내 PC에서는 되는데?" 문제 해결)
2.  **이기종(Polyglot) 서비스 확장 기반 마련:** 향후 Python(Flask/FastAPI)이나 Node.js 기반의 신규 서비스 추가 시, 컨테이너 네트워크 레벨에서 손쉽게 연동할 수 있는 뼈대를 구축합니다.
3.  **Kubernetes(K8s) 전환을 위한 예행연습:** K8s로의 이관을 대비하여 서비스 간 의존성과 네트워킹 구조를 컨테이너 관점에서 명확히 정의합니다.

---

## 2. 현재 상황 vs 추후 변화 (AS-IS vs TO-BE)

### 2.1. 현재 상황 (AS-IS)
*   **인프라 파편화:**
    *   `docker-compose.yml`에는 현재 `service-discovery` (Eureka) 컨테이너만 정의되어 있습니다.
    *   Auth Service 구동을 위해 필요한 인프라(Redis, H2/PostgreSQL)는 개발자가 각자 로컬에 띄워야 하거나 In-memory 모드로 동작 중입니다.
*   **설정의 하드코딩:**
    *   각 서비스의 `application.yml`에 의존성 주소(`localhost:6379`, `localhost:8761`)가 하드코딩되어 있어, 컨테이너 환경에서 네트워크 브릿지(Bridge) 문제나 DNS 해석(Resolution) 오류가 발생할 가능성이 높습니다.
*   **API Gateway 필터 미적용:**
    *   Gateway에서 JWT 필터링 등 실질적인 역할을 수행하지 못하고, 단순 라우팅만 처리하고 있습니다.

### 2.2. 추후 변화 (TO-BE)
*   **단일 진입점 (One-Click Start):**
    *   루트 디렉토리의 단일 `docker-compose.yml`을 통해 API Gateway, Service Discovery, Auth Service 및 기반 인프라(Redis, PostgreSQL)가 순차적으로 실행됩니다.
*   **도커 내부 네트워크 활용 (Container DNS):**
    *   각 애플리케이션은 `localhost`가 아닌 **컨테이너 이름(Service Name)**으로 서로를 호출합니다. (예: `http://service-discovery:8761`, `redis:6379`)
*   **환경 변수를 통한 설정 주입 (Externalized Config):**
    *   Spring Boot의 환경 변수 치환 기능을 활용하여 동적인 연결 정보 주입을 지원합니다.
*   **타 언어 서비스 연동 체계 마련:**
    *   추후 비(非) Spring 기반 서비스 추가 시, API Gateway에서 해당 컨테이너의 이름을 직접 라우팅(Direct Routing)하는 방식으로 연동할 수 있는 구조가 완성됩니다.

---

## 3. 구체적인 작업 지시사항 (Action Items)

작업 담당자는 아래의 단계별 지시사항을 따라 코드를 수정하고 Docker Compose 구성을 고도화해 주시기 바랍니다.

### Step 1: `docker-compose.yml` 고도화 (인프라 및 서비스 통합)
루트 디렉토리의 `docker-compose.yml`에 기존 `service-discovery` 외에 Redis, PostgreSQL, `api-gateway`, `auth-service`를 모두 추가합니다.

*   **요구사항:**
    1.  각 서비스 간 의존성을 정의(`depends_on`)하여 실행 순서를 보장합니다. (DB/Redis -> Service Discovery -> Gateway/Auth)
    2.  모든 컨테이너는 동일한 `microservices-network` (커스텀 브릿지 네트워크)에 속하도록 구성합니다.
    3.  Spring Boot 서비스들의 포트는 호스트 머신(Host) 포트와 컨테이너 포트를 매핑합니다. (예: `8080:8080`)

### Step 2: Spring Boot 설정 파일(`application.yml`) 수정 (환경 변수 적용)
각 서비스의 `application.yml`을 수정하여 하드코딩된 `localhost` 대신, Docker 컨테이너 이름을 참조하도록 변경합니다.

*   **API Gateway (`api-gateway/src/main/resources/application.yml`):**
    *   Eureka URL 수정: `defaultZone: ${EUREKA_URI:http://localhost:8761/eureka/}`
    *   Redis Host 수정: `host: ${REDIS_HOST:localhost}`
*   **Auth Service (`auth-service/src/main/resources/application.yml`):**
    *   Eureka URL 수정: `defaultZone: ${EUREKA_URI:http://localhost:8761/eureka/}`
    *   Redis Host 수정: `host: ${REDIS_HOST:localhost}`
    *   (선택) DB가 추가되었다면 DB 연결 정보 수정: `url: ${SPRING_DATASOURCE_URL:jdbc:h2:mem:testdb...}`

### Step 3: 각 서비스별 `Dockerfile` 생성
`api-gateway`와 `auth-service` 디렉토리 하위에 각각 해당 서비스를 빌드하고 실행할 수 있는 `Dockerfile`을 작성합니다.

*   **요구사항:**
    *   `openjdk:17-jdk-alpine` 또는 `eclipse-temurin:17-jre-alpine` 등 경량화된 베이스 이미지를 권장합니다.
    *   Gradle 빌드 산출물(`.jar`)을 복사하고 실행하는 명령어(`ENTRYPOINT`)를 포함해야 합니다.

### Step 4: 통합 테스트 및 동작 검증
1.  모든 수정이 완료되면 로컬에서 기존의 인스턴스(DB, Redis 등)를 모두 종료합니다.
2.  루트 디렉토리에서 프로젝트를 빌드합니다. (`./gradlew clean build -x test`)
3.  `docker-compose up --build -d` 명령어를 실행하여 전체 스택을 띄웁니다.
4.  아래의 항목들을 점검합니다.
    *   Eureka 대시보드(`http://localhost:8761`)에 `api-gateway`와 `auth-service`가 정상적으로 등록되었는지 확인
    *   API Gateway(`http://localhost:8080`)를 통해 Auth Service의 엔드포인트가 정상적으로 호출되는지 확인

---

## 4. 향후 타 언어(Python, Node.js) API 연동 전략 가이드

추후 AI 분석(Python)이나 실시간 통신(Node.js) 등의 타 언어 기반 마이크로서비스를 통합할 때는 아래의 원칙을 따릅니다.

1.  **동일 네트워크 합류:**
    신규 서비스용 컨테이너를 생성하고, 기존 `docker-compose.yml`의 `microservices-network`에 추가합니다.
2.  **직접 라우팅 (Direct Routing):**
    *   타 언어 서비스는 굳이 Eureka(Service Discovery)에 등록할 필요가 없습니다. (러닝 커브 및 아키텍처 복잡도 증가 방지)
    *   대신, `api-gateway`의 `application.yml` 라우팅 설정에 해당 컨테이너 이름을 직접 명시하여 프록시를 설정합니다.

    **(예시) Python AI 서비스 추가 시 Gateway 라우팅 설정:**
    ```yaml
    spring:
      cloud:
        gateway:
          routes:
            - id: python-ai-service
              uri: http://python-ai-service:5000 # Docker Compose 내 컨테이너 이름으로 찌름
              predicates:
                - Path=/ai/**
    ```

위 문서를 기반으로 단계적인 통합 작업을 진행해 주시기 바랍니다.