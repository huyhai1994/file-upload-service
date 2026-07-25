FROM amazoncorretto:17-alpine-jdk

LABEL maintainer="huyhai1994"

WORKDIR /app

ARG OTEL_AGENT_VERSION=2.30.0

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

COPY target/file_upload_service-0.0.1-SNAPSHOT.jar /app/app.jar

ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "/app/app.jar"]