# 마이크로 서비스 로그 설정 가이드

## 개요
이 프로젝트는 마이크로 서비스 아키텍처에서 중앙화된 로그 관리를 위한 설정을 포함하고 있습니다.

## 로그 설정 특징

### 1. 로그 레벨
- **INFO 레벨만 출력**: DEBUG, TRACE 레벨은 제외하여 로그 파일 크기 최적화
- **환경별 출력 방식**: 
  - 로컬 개발 환경: 콘솔 + 파일 출력
  - Docker 환경: 콘솔 출력만 (docker logs 명령어로 확인)

### 2. 파일 관리 (로컬 환경)
- **최대 파일 크기**: 10MB 단위로 파일 분할
- **시간 기반 로테이션**: 12시간 단위로 새 파일 생성
- **파일명 규칙**: `서비스명-YYYYMMDD-HH-순서.log`
  - 예: `service-discovery-20241201-14-0.log` (2024년 12월 1일 14시 첫 번째 파일)
- **보관 기간**: 30일
- **총 용량 제한**: 1GB

### 3. 환경별 로그 저장 방식
- **로컬 개발 환경**: `./logs/` (콘솔 + 파일 출력)
- **Docker 환경**: 콘솔 출력만 (docker logs로 확인)
- **자동 프로파일 전환**: Spring Profile 기반

## 설정된 서비스

### Service Discovery
- **로그 파일**: `service-discovery.log` (로컬 환경만)
- **설정 파일**: `service-discovery/src/main/resources/logback-spring.xml`
- **로컬 환경**: `./logs/service-discovery.log`
- **Docker 환경**: `docker logs` 명령어로 확인

## 환경별 설정

### 로컬 개발 환경
```bash
# 로컬에서 실행 시
./gradlew bootRun --args='--spring.profiles.active=local'

# 로그 파일 위치: ./logs/service-discovery.log
```

### Docker 환경
```bash
# Docker Compose 실행 시
docker-compose up -d

# 로그 확인: docker logs 명령어 사용
```

## 사용 방법

### 1. 로컬 개발
```bash
# 로컬에서 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 로그 확인
tail -f logs/service-discovery.log
```

### 2. Docker 환경
```bash
# Docker Compose 실행
docker-compose up -d

# 실시간 로그 확인
docker-compose logs -f service-discovery

# 특정 컨테이너 로그 확인
docker logs -f service-discovery
```

### 3. 로그 필터링 및 검색
```bash
# 에러 로그만 확인
docker-compose logs service-discovery | grep ERROR

# 특정 시간대 로그 확인
docker-compose logs --since="2024-12-01T10:00:00" service-discovery

# 로그 저장
docker-compose logs service-discovery > service-discovery.log
```

## 로그 패턴
```
2024-12-01 14:30:15.123 [main] INFO  c.m.s.ServiceDiscoveryApplication - Started ServiceDiscoveryApplication in 2.456 seconds
```

## 로그 파일 예시 (로컬 환경)
```
service-discovery-20241201-14-0.log  (12월 1일 14시 첫 번째 10MB)
service-discovery-20241201-14-1.log  (12월 1일 14시 두 번째 10MB)
service-discovery-20241201-02-0.log  (12월 1일 02시 첫 번째 10MB)
service-discovery-20241201-02-1.log  (12월 1일 02시 두 번째 10MB)
```

## 장점
1. **환경별 최적화**: 로컬에서는 파일 저장, Docker에서는 콘솔 출력
2. **권한 문제 해결**: Docker 환경에서 파일 시스템 권한 문제 완전 해결
3. **간단한 관리**: Docker 환경에서는 별도 볼륨 관리 불필요
4. **표준 방식**: Docker의 표준 로그 관리 방식 사용
5. **효율적인 로테이션**: 로컬 환경에서 12시간 단위로 적절한 파일 분할
6. **실시간 모니터링**: `docker logs -f` 명령어로 실시간 로그 확인 가능

## 주의사항
1. 로컬 환경의 로그 파일은 자동으로 로테이션되므로 수동 삭제는 권장하지 않습니다.
2. Docker 환경의 로그는 컨테이너 삭제 시 함께 삭제됩니다.
3. 로컬 개발 시 `./logs` 디렉토리가 자동으로 생성됩니다.
4. Docker 환경에서는 `docker logs` 명령어를 사용하여 로그를 확인하세요. 