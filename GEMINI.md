# Project: micro-service (Spring Cloud MSA)

이 프로젝트는 Spring Cloud 기반의 마이크로서비스 아키텍처(MSA) 실습 및 구축 프로젝트입니다. API Gateway, Service Discovery, Auth Service 등으로 구성되어 있으며, Docker Compose를 통한 통합 개발 환경을 지향합니다.

## 🏗 Architecture Overview

- **Service Discovery**: Eureka를 사용하여 서비스 인스턴스를 관리합니다.
- **API Gateway**: 모든 서비스의 단일 진입점이며, 라우팅 및 보안(JWT) 필터링을 담당합니다.
- **Auth Service**: 사용자 인증 및 인가, 토큰 발급을 담당하며 PostgreSQL과 Redis(캐싱/세션)를 사용합니다.
- **Common Libraries**: 예외 처리(`common-exceptions`) 및 JWT 유틸리티(`common-jwt`)를 공통 모듈로 분리하여 재사용합니다.
- **Configuration**: 외부 `config-server` 컨테이너와 연동하여 환경 설정을 중앙 관리합니다.

## 🛠 Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.4.5, Spring Cloud 2023.x (or compatible)
- **Build Tool**: Gradle 8.8
- **Database**: PostgreSQL 15 (Auth DB)
- **Cache**: Redis 7.4.8 (Token/Session)
- **DevOps**: Docker, Docker Compose
- **Security**: Spring Security, JWT (JJWT 0.12.x)

## 📁 Project Structure

- `api-gateway/`: Spring Cloud Gateway 서비스
- `auth-service/`: 인증 및 인가 서비스
- `service-discovery/`: Eureka Server (Discovery 서비스)
- `common-exceptions/`: 공통 예외 처리 라이브러리 (Plain Java)
- `common-jwt/`: JWT 생성 및 검증 라이브러리 (Plain Java)
- `docker-compose.yml`: 전체 서비스 및 인프라 통합 실행 설정
- `architecture.drawio`: 시스템 아키텍처 설계도

## 📜 Development Rules & Principles

### 1. MSA & Docker Principles
- **Container DNS**: 서비스 간 통신 시 `localhost`가 아닌 컨테이너 이름(예: `http://auth-service:8081`)을 사용합니다.
- **Externalized Config**: 모든 설정값은 `application.yml`에 하드코딩하지 않고 환경 변수(`${VAR:default}`)를 통해 주입받습니다.
- **Non-root Containers**: Dockerfile 작성 시 보안을 위해 `admin` 계정 등 비루트(non-root) 계정으로 실행하도록 구성합니다.
- **Lightweight Images**: `eclipse-temurin:17-jre-alpine`과 같은 경량 베이스 이미지를 사용합니다.

### 2. Coding Standards
- **Java Version**: 반드시 Java 17 문법을 준수합니다.
- **Spring Boot**: 3.x 버전의 최신 관례를 따릅니다.
- **JWT Security**: JWT Secret Key는 반드시 256비트(32자) 이상이어야 합니다.
- **Testing**: 모든 신규 기능 및 버그 수정은 JUnit 5를 활용한 테스트 코드를 동반해야 합니다.

### 3. Git & Documentation
- **Commit Messages**: 한글 또는 영문을 사용하되, 목적이 명확해야 합니다.
- **Docs Update**: 아키텍처나 인프라 변경 시 `TECH_NOTE_MSA_DOCKER_MIGRATION.md`와 같은 문서를 최신화합니다.

## 🚀 Getting Started (Docker Compose)

```bash
# 전체 프로젝트 빌드
./gradlew clean build -x test

# 인프라 및 서비스 구동
docker-compose up -d

# (중요) 외부 Config Server 네트워크 연동 (필요 시)
docker network connect micro-service_microservices-network config-server
docker-compose restart auth-service api-gateway
```

---
*Last Updated: 2026-04-21*
