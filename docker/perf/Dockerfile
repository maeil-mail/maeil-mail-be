FROM openjdk:17-slim AS build
WORKDIR /app
COPY ../../ .
RUN apt-get update && apt-get install -y curl
RUN curl -Lo dd-java-agent.jar 'https://dtdg.co/latest-java-tracer'
RUN ./gradlew clean build --no-daemon

FROM gcr.io/distroless/java17-debian12
WORKDIR /app
COPY --from=build /app/maeil-mail/build/libs/*.jar perf-test-app.jar
COPY --from=build /app/dd-java-agent.jar dd-java-agent.jar
ENTRYPOINT [ \
    "java", \
    "-javaagent:./dd-java-agent.jar", \
    "-Xms512m", \
    "-Xmx512m", \
    "-XX:+UseG1GC", \
    "-XX:+UseContainerSupport", \
    "-XX:FlightRecorderOptions=stackdepth=256", \
    "-jar", "perf-test-app.jar" \
]
