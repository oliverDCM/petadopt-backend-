# syntax=docker/dockerfile:1

# ---------- Etapa 1: compilar (JDK + Maven; no llega a la imagen final) ----------
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Primero solo lo que define las dependencias: Docker reutiliza esta capa
# mientras el pom.xml no cambie, y las compilaciones siguientes son mucho más rápidas.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Evita fallos si el archivo se guardó con saltos de línea de Windows (CRLF)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw \
    && ./mvnw -B -q dependency:go-offline

COPY src ./src
RUN ./mvnw -B -q package -DskipTests

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
