FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S admin && adduser -S admin -G admin
RUN mkdir -p /app/logs && chown -R admin:admin /app && chmod 755 /app/logs
COPY build/libs/*-SNAPSHOT.jar ./app.jar
USER admin
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
