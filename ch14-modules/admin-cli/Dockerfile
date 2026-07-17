FROM eclipse-temurin:25-jdk-alpine
WORKDIR app
COPY build/dist/dependencies/ ./
COPY build/dist/spring-boot-loader/ ./
COPY build/dist/snapshot-dependencies/ ./
COPY build/dist/application/ ./
ENTRYPOINT ["java", "-jar", "admin-cli-1.0.jar"]
