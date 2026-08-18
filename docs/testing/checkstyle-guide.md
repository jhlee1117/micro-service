# Google Checkstyle Guide

이 프로젝트는 Gradle `checkstyle` 플러그인과 `config/checkstyle` 아래의 설정 파일을 사용합니다.

## Configuration

- `config/checkstyle/google_checks.xml`: Google Java Style 기반 Checkstyle 규칙
- `config/checkstyle/suppressions.xml`: 프로젝트별 예외 규칙

기본 규칙을 조정해야 하면 `google_checks.xml`을 수정합니다. 특정 파일이나 규칙만 예외 처리해야 하면 `suppressions.xml`에 추가합니다.

## Run

전체 메인 소스 검사:

```bash
./gradlew checkstyleMain
```

테스트 소스까지 검사:

```bash
./gradlew checkstyleMain checkstyleTest
```

Gradle `check`를 실행하면 테스트와 Checkstyle 검사가 함께 수행됩니다.

```bash
./gradlew check
```

## Reports

각 모듈의 리포트는 다음 위치에 생성됩니다.

```text
<module>/build/reports/checkstyle/main.html
<module>/build/reports/checkstyle/test.html
```

## Notes

- Checkstyle 대상은 각 모듈의 `src/main/java`, `src/test/java`입니다.
- QueryDSL 등 빌드 중 생성되는 Java 파일은 검사 대상에서 제외됩니다.
- 현재 코드베이스는 Google Java Style에 맞춰 일괄 정리되지 않았으므로, 초기 실행 시 기존 위반이 다수 보고될 수 있습니다.
