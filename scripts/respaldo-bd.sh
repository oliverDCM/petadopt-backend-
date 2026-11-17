#!/usr/bin/env bash
# respaldo-bd.sh — respaldo de la base de datos de PetAdopt (anexo, paso 17).
#   On-premise: pg_dump dentro del contenedor db.
#   Cloud:      pg_dump desde un contenedor temporal postgres:18 hacia RDS (la base no es pública:
#               solo la EC2 puede conectarse, por el grupo de seguridad).
#
#   ./scripts/respaldo-bd.sh       crea respaldos/petadopt_<ambiente>_<fecha>.dump y su .sha256
#
# La contraseña se lee del .env y se pasa como variable de entorno, nunca en la línea de comandos.
# Los respaldos tienen datos de usuarios: no se suben a GitHub (respaldos/ está en .gitignore).
set -euo pipefail
source "$(dirname "$0")/comun.sh"

mkdir -p respaldos
chmod 700 respaldos
ARCHIVO="respaldos/petadopt_${AMBIENTE}_$(date +%Y%m%d_%H%M%S).dump"
PGPASSWORD="$(leer_env DB_PASSWORD "$(leer_env POSTGRES_PASSWORD)")"
export PGPASSWORD

echo "Respaldando la base '$DB_NAME' ($AMBIENTE)..."
if [ "$AMBIENTE" = "on-premise" ]; then
    $DOCKER compose exec -T -e PGPASSWORD db pg_dump -U "$DB_USER" -d "$DB_NAME" -Fc --no-owner > "$ARCHIVO"
else
    $DOCKER run --rm -e PGPASSWORD -e PGSSLMODE=require postgres:18-alpine \
        pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -Fc --no-owner > "$ARCHIVO"
fi

sha256sum "$ARCHIVO" > "$ARCHIVO.sha256"
chmod 600 "$ARCHIVO" "$ARCHIVO.sha256"
echo "Listo: $ARCHIVO ($(du -h "$ARCHIVO" | cut -f1))"
echo "Huella SHA-256: $(cut -d' ' -f1 "$ARCHIVO.sha256")"
echo "Contenido (tablas incluidas):"
$DOCKER run --rm -i postgres:18-alpine pg_restore --list < "$ARCHIVO" | grep -E 'TABLE DATA' | awk '{print "  - " $(NF-1)}'
echo
echo "Para comprobar que sirve: ./scripts/restaurar-bd.sh $ARCHIVO"
