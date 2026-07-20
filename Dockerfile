FROM amazoncorretto:17-alpine-jdk

LABEL maintainer="huyhai1994"

WORKDIR /app

COPY target/file_upload_service-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]