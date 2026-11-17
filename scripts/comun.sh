#!/usr/bin/env bash
# Funciones compartidas por los scripts de operación. No se ejecuta solo: lo cargan los demás con "source".

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$RAIZ" || exit 1

# Lee una variable del archivo .env sin ejecutarlo (los valores pueden tener espacios o símbolos).
#   leer_env NOMBRE [valor_por_defecto]
leer_env() {
    local valor=""
    if [ -f "$RAIZ/.env" ]; then
        valor="$(grep -E "^[[:space:]]*$1=" "$RAIZ/.env" | tail -n 1 | cut -d= -f2- | sed -e 's/^[\"'\'']//' -e 's/[\"'\'']$//' -e 's/\r$//')"
    fi
    if [ -z "$valor" ]; then valor="${2:-}"; fi
    printf '%s' "$valor"
}

PUERTO_APP="$(leer_env PUERTO_APP 4200)"
URL_LOCAL="http://127.0.0.1:${PUERTO_APP}"
DB_HOST="$(leer_env DB_HOST db)"
DB_PORT="$(leer_env DB_PORT 5432)"
DB_NAME="$(leer_env DB_NAME "$(leer_env POSTGRES_DB petadopt)")"
DB_USER="$(leer_env DB_USER "$(leer_env POSTGRES_USER petadopt)")"

# Ambiente según dónde está la base: contenedor local (on-premise) o un servidor externo (RDS en cloud)
if [ "$DB_HOST" = "db" ]; then AMBIENTE="on-premise"; else AMBIENTE="cloud"; fi

# docker con sudo solo si el usuario no pertenece al grupo docker
if docker info >/dev/null 2>&1; then DOCKER="docker"; else DOCKER="sudo docker"; fi
