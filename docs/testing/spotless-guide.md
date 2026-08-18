# Spotless Formatting Guide

This project uses Spotless with google-java-format for Java source formatting.

## Configuration

- Gradle plugin: `com.diffplug.spotless`
- Formatter: `googleJavaFormat('1.28.0')`
- Java baseline: Java 17
- Targets:
  - `src/main/java/**/*.java`
  - `src/test/java/**/*.java`

The formatter uses Google Java Format's default Java indentation. Keep
`.editorconfig` Java indentation at 2 spaces so editors do not fight the
formatter.

## Commands

Apply formatting:

```bash
./gradlew spotlessApply
```

Check formatting:

```bash
./gradlew spotlessCheck
```

Run formatting and Checkstyle verification:

```bash
./gradlew spotlessCheck checkstyleMain checkstyleTest
```

Run formatting, Checkstyle, and architecture tests:

```bash
./gradlew spotlessCheck checkstyleMain checkstyleTest test
```

## Notes

- `spotlessApply` is a mechanical source formatting operation.
- `google-java-format` does not replace Checkstyle. Checkstyle still enforces
  project lint rules from `config/checkstyle`.
- JavaDoc formatting is disabled in Spotless because public class and method
  JavaDoc are not mandatory in this project.
