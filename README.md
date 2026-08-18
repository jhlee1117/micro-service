# micro-service

Spring Cloud 기반 SaaS 멀티테넌트 HR 백엔드 학습 프로젝트입니다. Gradle 멀티 모듈로 구성되어 있으며 API Gateway, 인증 서비스, 게시판 서비스, 공통 JWT/예외/유틸 모듈, 테넌트 프로비저닝 워커를 포함합니다.

## 주요 모듈

- `api-gateway`: Spring Cloud Gateway 진입점 및 JWT 검증
- `auth-service`: 인증, 사용자, 테넌트, 권한, 메뉴 관리
- `board-service`: 게시판 예제 서비스
- `tenant-provisioning-worker`: RabbitMQ 기반 테넌트 스키마 프로비저닝
- `common-jwt`: JWT 생성/검증 및 인증 필터 공통 라이브러리
- `common-exceptions`: 공통 예외 및 응답 규격
- `common-util`: 공통 유틸리티

## 문서

문서는 `docs` 아래 역할별로 정리합니다.

- `docs/architecture`: 시스템 구조, 이벤트 흐름, 다이어그램
- `docs/adr`: 아키텍처 의사결정 기록
- `docs/api`: API 명세
- `docs/database`: ERD 및 테이블 정의
- `docs/deployment`: Docker Compose, 배포, 운영 가이드
- `docs/testing`: 테스트 및 검증 절차

AI Agent 작업 지침은 `AGENTS.md`를 우선 읽습니다.

## 로컬 실행

```bash
./gradlew clean build
docker-compose up -d --build
```

주요 접속 정보:

- API Gateway: `http://localhost:8080`
- Eureka Dashboard: `http://localhost:8761`
- RabbitMQ UI: `http://localhost:15672`
