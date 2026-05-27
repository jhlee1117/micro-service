---
name: add-new-service
description: Spring Cloud MSA 프로젝트에 새로운 Spring Boot 서비스를 추가합니다. 디렉토리 구조 생성, Gradle 등록, Dockerfile 작성, docker-compose.yml 업데이트를 포함한 전체 과정을 자동화합니다.
---

# Skill: Add New MSA Service

이 스킬은 프로젝트의 표준 아키텍처(Java 17, Spring Boot 3.4.5, Docker Alpine)에 맞는 새로운 마이크로서비스 모듈을 생성합니다.

## Procedures

### 1. 서비스 정보 수집
- 서비스 이름 (예: `order-service`, `product-service`)
- 포트 번호 (예: `8082`, `8083`)
- 기본 패키지명 (기본값: `com.microservices.<service_name_underscore>`)

### 2. 디렉토리 및 소스 구조 생성
- `src/main/java/<package_path>/<ClassName>Application.java` 생성
- `src/main/resources/application.yml` 생성 (Eureka Client 설정 포함)
- `src/main/resources/application-docker.yml` 생성 (컨테이너 전용 설정)

### 3. 프로젝트 설정 파일 생성
- `build.gradle`: Spring Boot, Cloud Starter, Eureka Client 의존성 포함
- `Dockerfile`: `eclipse-temurin:17-jre-alpine` 기반, 비루트(`admin`) 계정 설정 포함

### 4. 루트 설정 업데이트
- `settings.gradle`: `include('<service_name>')` 추가
- `docker-compose.yml`: 신규 서비스 정의 추가 (네트워크, 환경 변수, 의존성 설정)

## Templates

### Dockerfile Template
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S admin && adduser -S admin -G admin
RUN mkdir -p /app/logs && chown -R admin:admin /app && chmod 755 /app/logs
COPY build/libs/*-SNAPSHOT.jar ./app.jar
USER admin
EXPOSE <PORT>
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### application.yml (Eureka Client)
```yaml
spring:
  application:
    name: <SERVICE_NAME>
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URI:http://localhost:8761/eureka/}
```

## Instructions for Agent
1. 사용자가 서비스 이름과 포트 번호를 입력하면 위 절차에 따라 파일을 생성하십시오.
2. 모든 파일 생성 후 `./gradlew build -x test`를 실행하여 빌드 성공 여부를 확인하십시오.
3. 마지막으로 `docker-compose.yml`에 서비스가 올바르게 추가되었는지 검증하십시오.
