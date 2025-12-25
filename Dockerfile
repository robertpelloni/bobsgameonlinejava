FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :server:installDist -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/server/build/install/server /app/server
COPY entrypoint.sh /app/
RUN chmod +x /app/entrypoint.sh

EXPOSE 4444 4445 4446/udp

ENTRYPOINT ["/app/entrypoint.sh"]
