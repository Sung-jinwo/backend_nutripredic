# 🐳 NutriPredic Backend - Docker Deployment

## 📋 Requisitos Previos

- Docker Engine 24+
- Docker Compose v2+
- Java 21 (solo para desarrollo local, no en contenedor)

## 🚀 Inicio Rápido

### 1. Configurar variables de entorno

```bash
# Copiar plantilla
cp .env.example .env

# Editar valores críticos (OBLIGATORIO en producción)
# - POSTGRES_PASSWORD
# - JWT_SECRET (mínimo 32 chars: openssl rand -base64 32)
# - API_KEY_GEMINI (si usas Gemini)
# - ADMIN_PASSWORD
```

### 2. Construir y levantar servicios

```bash
# Desarrollo (con base de datos + backend)
docker compose up --build -d

# Ver logs
docker compose logs -f backend

# Solo base de datos
docker compose up -d postgres
```

### 3. Verificar salud

```bash
# Health check del backend
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html

# Base de datos
docker compose exec postgres pg_isready -U postgres -d nutripredic
```

## 🏗️ Estructura de Servicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| `postgres` | 5432 | Base de datos PostgreSQL 17 |
| `backend` | 8080 | API Spring Boot (NutriPredic) |
| `ai` | 8000 | Servicio IA Python (opcional, `profile: ai`) |

## 🔧 Comandos Útiles

```bash
# Ver logs en tiempo real
docker compose logs -f backend

# Rebuild solo backend tras cambios de código
docker compose build backend && docker compose up -d backend

# Ejecutar migraciones Flyway manualmente
docker compose exec backend java -jar /app/app.jar flyway:migrate

# Acceso a shell del backend
docker compose exec backend sh

# Backup base de datos
docker compose exec postgres pg_dump -U postgres nutripredic > backup.sql

# Restaurar backup
docker compose exec -T postgres psql -U postgres nutripredic < backup.sql

# Limpiar todo (¡CUIDADO: borra datos!)
docker compose down -v
```

## 🌍 Variables Críticas de Producción

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `POSTGRES_PASSWORD` | Password BD | `password_seguro_123` |
| `JWT_SECRET` | Clave JWT (32+ chars) | `openssl rand -base64 32` |
| `API_KEY_GEMINI` | API Key Google AI | `AIza...` |
| `ADMIN_PASSWORD` | Password admin | `PasswordSeguro123!` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos | `https://app.tudominio.com` |

## 🏥 Health Checks

```bash
# Backend
curl http://localhost:8080/actuator/health

# PostgreSQL
docker compose exec postgres pg_isready -U postgres -d nutripredic
```

## 📦 Build para Producción

```bash
# Build multi-stage (optimizado)
docker compose -f docker-compose.yml build

# Push a registry (ej. Docker Hub, GHCR, ECR)
docker tag nutripredic-backend tu-usuario/nutripredic:v1.0.0
docker push tu-usuario/nutripredic:v1.0.0
```

## 🔐 Secrets en Producción

**NUNCA** pongas secrets en el código o docker-compose.yml. Usa:

- **Docker Secrets**: `docker secret create jwt_secret ./jwt_secret.txt`
- **Variables de entorno** en el orquestador (K8s, ECS, Portainer, Coolify)
- **Vault / AWS Secrets Manager / Azure Key Vault**

## 📊 Monitoreo

```bash
# Stats de contenedores
docker stats

# Logs estructurados (JSON)
docker compose logs --tail=100 backend | jq .
```

## 🚨 Troubleshooting

| Problema | Solución |
|----------|----------|
| `Connection refused` DB | `docker compose up -d postgres` y esperar healthcheck |
| `Flyway validation failed` | `docker compose exec backend java -jar app.jar flyway:repair` |
| `JWT_SECRET too short` | Mínimo 32 chars: `openssl rand -base64 32` |
| `Port 8080 in use` | Cambia `ports:` en docker-compose.yml |
| OOM / Memoria | Aumenta `JAVA_OPTS: "-XX:MaxRAMPercentage=80.0"` |

## 📝 Notas

- **Flyway** ejecuta migraciones automáticamente al inicio
- **H2** solo en tests (`scope: test`), producción usa PostgreSQL
- **JAR** generado con `mvn package -DskipTests` (multi-stage Dockerfile)
- **Usuario no-root** en contenedor (UID 1000)
- **Healthcheck** cada 30s en backend y PostgreSQL