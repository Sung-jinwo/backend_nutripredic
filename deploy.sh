#!/bin/bash
# ============================================
# Script de despliegue para NutriPredic Backend
# Uso: ./deploy.sh [dev|prod] [tag]
# ============================================

set -euo pipefail

ENV="${1:-dev}"
TAG="${2:-latest}"
IMAGE_NAME="nutripredic-backend"
FULL_IMAGE_NAME="nutripredic-backend:${TAG}"

echo "🚀 Desplegando NutriPredic Backend"
echo "   Entorno: ${ENV}"
echo "   Tag: ${TAG}"
echo "   Imagen: ${FULL_IMAGE_NAME}"

# Verificar .env
if [[ ! -f .env ]]; then
    echo "❌ Archivo .env no encontrado. Copia .env.example a .env y configura."
    exit 1
fi

# Cargar variables
set -a
source .env
set +a

# Validar variables críticas
if [[ -z "${JWT_SECRET}" || ${#JWT_SECRET} -lt 32 ]]; then
    echo "❌ JWT_SECRET debe tener al menos 32 caracteres"
    exit 1
fi

if [[ -z "${POSTGRES_PASSWORD}" ]]; then
    echo "❌ POSTGRES_PASSWORD no configurado"
    exit 1
fi

echo "✅ Variables de entorno validadas"

# Build imagen
echo "🔨 Construyendo imagen Docker..."
docker compose build backend

if [[ "${ENV}" == "prod" ]]; then
    echo "🏷️  Etiquetando para producción..."
    docker tag nutripredic-backend:latest "${IMAGE_NAME}:${TAG}"
    # docker push tu-registry/nutripredic-backend:${TAG}
    echo "✅ Imagen lista para push a registry"
fi

# Levantar servicios
echo "🚀 Levantando servicios..."
if [[ "${ENV}" == "prod" ]]; then
    docker compose -f docker-compose.yml up -d
else
    docker compose up -d
fi

# Esperar healthchecks
echo "⏳ Esperando healthchecks..."
sleep 10

# Verificar health
for i in {1..30}; do
    if curl -sf http://localhost:8080/actuator/health | grep -q '"status":"UP"'; then
        echo "✅ Backend healthy!"
        break
    fi
    if [[ $i -eq 30 ]]; then
        echo "❌ Timeout esperando healthcheck"
        docker compose logs backend --tail=50
        exit 1
    fi
    sleep 2
done

echo ""
echo "✅ Despliegue completado"
echo "   Backend: http://localhost:8080"
echo "   Swagger: http://localhost:8080/swagger-ui.html"
echo "   Health:  http://localhost:8080/actuator/health"
echo ""
echo "📊 Logs: docker compose logs -f backend"