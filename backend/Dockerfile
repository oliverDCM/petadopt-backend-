# syntax=docker/dockerfile:1

# ---------- Etapa 1: compilar (Maven + JDK; no llega a la imagen final) ----------
# Imagen oficial con Maven ya instalado: no hay que descargar Maven con mvnw durante el build.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Fuerza IPv4: con IPv6 sin salida a internet, Docker falla con "Network unreachable"
ENV MAVEN_OPTS="-Djava.net.preferIPv4Stack=true"

# Primero solo el pom: Docker reutiliza esta capa (dependencias) mientras el pom.xml no cambie
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecutar (solo JRE) ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Usuario sin privilegios: no se ejecuta como root
RUN addgroup -S petadopt && adduser -S petadopt -G petadopt

COPY --from=build /app/target/*.jar app.jar

USER petadopt
EXPOSE 8080

# Opciones de la JVM; se pueden cambiar con la variable de entorno JAVA_OPTS
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -q --spider http://127.0.0.1:8080/actuator/health || exit 1

# "exec" para que la JVM reciba las señales de parada (docker stop)
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
