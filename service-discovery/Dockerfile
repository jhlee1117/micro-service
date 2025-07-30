FROM gradle:8.5-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

RUN gradle dependencies --no-daemon

COPY src ./src

RUN gradle build --no-daemon

FROM openjdk:17-jdk-slim

WORKDIR /app

RUN groupadd -r admin && useradd -r -g admin admin

COPY --from=build /app/build/libs/*.jar ./app.jar

# 로그 디렉토리 생성 및 권한 설정
RUN mkdir -p /app/logs && \
    chown -R admin:admin /app && \
    chmod 755 /app/logs

USER admin

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
