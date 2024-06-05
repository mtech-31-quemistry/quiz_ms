# Build and Generate Jar Stage
FROM gradle:8.7.0-jdk21 AS project-build
COPY . .

RUN gradle bootJar

RUN mv build/libs/quiz_ms.jar /usr/share/quiz_ms.jar
RUN mv src/main/resources/application.yml /usr/share/application.yml


# Custom Java runtime using jlink in a multi-stage container build
FROM eclipse-temurin:21 as jre-build
# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules jdk.unsupported,java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime
# Define your base image
FROM debian:stretch-slim
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

COPY --from=project-build /usr/share/quiz_ms.jar .
COPY --from=project-build /usr/share/application.yml .

EXPOSE 8080
CMD ["java", "-jar", "quiz_ms.jar"]
