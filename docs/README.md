# Project Documents

이 디렉터리는 프로젝트 문서를 주제별로 관리합니다. 새 문서를 추가할 때는 아래 분류 중 하나에 넣고, 이름은 소문자 kebab-case를 사용합니다.

## Structure

- `architecture/`: 시스템 구조, 서비스 간 이벤트 흐름, Draw.io 및 아키텍처 이미지
- `adr/`: 주요 기술 결정과 근거를 기록하는 Architecture Decision Record
- `api/`: REST API, 인증 흐름, 요청/응답 명세
- `database/`: ERD, 테이블 정의, 스키마 설계
- `deployment/`: Docker Compose, 컨테이너, 로그, 배포 및 운영 절차
- `testing/`: 통합 테스트, 수동 검증 절차, 테스트 시나리오

## Current Documents

- `architecture/tenant-event-design.md`: 테넌트 이벤트 기반 프로비저닝 설계
- `architecture/architecture.drawio`: 전체 시스템 아키텍처 Draw.io 원본
- `architecture/architecture_docker_compose.drawio`: Docker Compose 아키텍처 Draw.io 원본
- `architecture/architecture_docker_compose.png`: Docker Compose 아키텍처 이미지
- `architecture/common-jwt-analysis.md`: 공통 JWT 라이브러리 구조 및 유의점 분석
- `architecture/common-jwt-flow.mmd`: 공통 JWT 인증 흐름 Mermaid 다이어그램
- `architecture/tenant-schema-routing.md`: 로그인 이후 테넌트 스키마 라우팅 및 SUPER_ADMIN 전체 접근 구조
- `architecture/archunit-rules.md`: 모듈별 ArchUnit 아키텍처 규칙 (새 패키지 추가 전 필독)
- `architecture/system-architecture.png`: 시스템 아키텍처 이미지
- `database/db_relationship.md`: 데이터베이스 ERD 및 테이블 정의
- `database/db_relationship.drawio`: DB 관계도 Draw.io 원본
- `api/oauth-login-flow.md`: OAuth 로그인 처리 방향 및 Spring Security OAuth2 흐름
- `api/rate-limit.md`: Redis 기반 로그인 Rate Limit API 및 운영 방식
- `deployment/log-setup-guide.md`: 로그 설정 가이드
- `deployment/add-new-service-guide.md`: 신규 Spring Boot MSA 서비스 추가 절차
- `deployment/msa-docker-compose-guide.md`: Docker Compose 통합 가이드
- `deployment/msa-docker-migration-tech-note.md`: Docker Compose 마이그레이션 기술 노트
- `deployment/msa-infra-agent-notes.md`: 인프라 작업용 Agent 참고 노트
- `testing/checkstyle-guide.md`: Google Checkstyle 실행 및 리포트 확인 방법
- `testing/jwt-integration-guide.md`: JWT 통합 테스트 가이드

## Archive Policy

런타임 로그, 에디터 백업, 깨진 인코딩 문서, Spring Initializr 기본 HELP 문서는 루트의 `archive/YYYY-MM-DD` 아래로 이동해 보관합니다. 개발 또는 리뷰 시에는 `docs`와 `AGENTS.md`를 우선 참조합니다.
