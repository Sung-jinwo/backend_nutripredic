# ============================================
# Dockerfile multi-stage para NutriPredic Backend
# ============================================

# ---- STAGE 1: Build con Maven ----
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar solo pom.xml primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn package -DskipTests -B

# ---- STAGE 2: Runtime con JRE ligero ----
FROM eclipse-temurin:21-jre-alpine

# Instalar curl para healthchecks
RUN apk add --no-cache curl

# Usuario no-root para seguridad
RUN addgroup -g 1000 -S nutripredic && \
    adduser -u 1000 -S nutripredic -G nutripredic

WORKDIR /app

# Copiar JAR compilado
COPY --from=builder /app/target/nutri_predic-*.jar /app/app.jar

# Cambiar propietario
RUN chown nutripredic:nutripredic /app/app.jar

USER nutripredic

# Exponer puerto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Variables de entorno por defecto (sobrescribibles en docker-compose)
ENV SPRING_PROFILES_ACTIVE=docker \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0" \
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/nutripredic \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=postgres \
    SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
    SPRING_FLYWAY_ENABLED=true \
    SPRING_FLYWAY_LOCATIONS=classpath:db/migration \
    SPRING_FLYWAY_BASELINE_ON_MIGRATE=true \
    SPRING_JPA_OPEN_IN_VIEW=false \
    JWT_SECRET="" \
    JWT_EXPIRATION_MS=86400000 \
    CORS_ALLOWED_ORIGINS="http://localhost:5173" \
    AI_SERVICE_URL=http://ai:8000/predict \
    ADMIN_EMAIL=admin@nutripredic.local \
    ADMIN_PASSWORD=ChangeMe123! \
    ADMIN_NAME=Administrador \
    API_KEY_GEMINI="" \
    GEMINI_BASE_URL=https://generativelanguage.googleapis.com \
    GEMINI_MODEL=gemini-3.1-flash-lite \
    GEMINI_TIMEOUT=PT30S \
    GEMINI_MOCK_ENABLED=false

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
