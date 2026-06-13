# ─── Stage 1: Build ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
COPY src src
RUN ./mvnw package -DskipTests -B

# ─── Stage 2: Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Non-root user/group (security baseline; satisfies Pod Security restricted profile)
RUN addgroup -S gbogroup && adduser -S gbouser -G gbogroup -u 1001

WORKDIR /app

# Application JAR
COPY --from=build /app/target/*.jar app.jar

# RDS CA bundle for SSL/TLS verification (sslmode=verify-full).
# Before building the image, download the bundle:
#   curl -o src/main/resources/ssl/rds-ca-bundle.pem \
#     https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem
# A placeholder file lives under src/main/resources/ssl/.gitkeep so the COPY
# does not fail when the bundle has not yet been downloaded for local builds.
COPY src/main/resources/ssl /etc/ssl/certs/rds

# Required writable mount points — the container's root filesystem is read-only
# in production (see k8s/05-deployment.yaml securityContext.readOnlyRootFilesystem).
# These directories are mounted as emptyDir volumes by Kubernetes.
RUN mkdir -p /tmp/tomcat /tmp/heapdump && \
    chown -R gbouser:gbogroup /app /tmp/tomcat /tmp/heapdump

USER gbouser

EXPOSE 8080

# JVM tuning: container-aware heap, G1GC for low-latency finance workloads,
# JNDI/LDAP injection mitigations, and explicit error/heap-dump paths.
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:InitialRAMPercentage=50.0", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:+HeapDumpOnOutOfMemoryError", \
  "-XX:HeapDumpPath=/tmp/heapdump", \
  "-XX:ErrorFile=/tmp/heapdump/hs_err_pid%p.log", \
  "-Dcom.sun.jndi.ldap.object.trustURLCodebase=false", \
  "-Dcom.sun.jndi.rmi.object.trustURLCodebase=false", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
