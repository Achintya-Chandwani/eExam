# ─────────────────────────────────────────
# Stage 1 — Build the JAR with Maven
# ─────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy the Maven POM and download dependencies first (layer caching)
COPY eexam-platform/pom.xml ./pom.xml
RUN mvn dependency:go-offline -q

# Copy source code and build
COPY eexam-platform/src ./src
RUN mvn clean package -DskipTests -q

# ─────────────────────────────────────────
# Stage 2 — Run with a slim JRE image
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S eexam && adduser -S eexam -G eexam

# Copy only the built JAR from Stage 1
COPY --from=builder /build/target/eexam-platform.jar app.jar

# Set ownership
RUN chown eexam:eexam app.jar
USER eexam

# Expose the default Spring Boot port
EXPOSE 8080

# Health check (Render uses this)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the app — DB config is injected via environment variables on Render
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
