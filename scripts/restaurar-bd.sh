#!/usr/bin/env bash
# restaurar-bd.sh — restaura un respaldo en un AMBIENTE DE PRUEBA y lo compara con la base real (anexo, paso 17).
#
#   ./scripts/restaurar-bd.sh respaldos/petadopt_xxx.dump
#       1. verifica la huella SHA-256 del archivo
#       2. crea un PostgreSQL temporal (contenedor petadopt-restauracion, sin puertos publicados)
#       3. restaura el respaldo allí y cuenta las filas de cada tabla
#       4. compara con la base real y borra el contenedor temporal
#
#   ./scripts/restaurar-bd.sh respaldos/petadopt_xxx.dump --sobre-la-real
#       Recuperación real ante un desastre: reemplaza el contenido de la base del ambiente actual.
#       Pide confirmación escribiendo RESTAURAR. Detén antes las réplicas: docker compose stop backend1 backend2
set -euo pipefail
source "$(dirname "$0")/comun.sh"

ARCHIVO="${1:?Uso: ./scripts/restaurar-bd.sh respaldos/archivo.dump [--sobre-la-real]}"
[ -f "$ARCHIVO" ] || { echo "No existe $ARCHIVO"; exit 1; }
if [ -f "$ARCHIVO.sha256" ]; then
    sha256sum -c "$ARCHIVO.sha256" || { echo "La huella no coincide: el respaldo está dañado"; exit 1; }
fi
PGPASSWORD="$(leer_env DB_PASSWORD "$(leer_env POSTGRES_PASSWORD)")"
export PGPASSWORD
TABLAS="usuarios animales solicitudes historial_solicitud tokens_recuperacion"

consulta_real() {   # ejecuta SQL en la base real del ambiente
    if [ "$AMBIENTE" = "on-premise" ]; then
        $DOCKER compose exec -T -e PGPASSWORD db psql -U "$DB_USER" -d "$DB_NAME" -Atc "$1"
    else
        $DOCKER run --rm -e PGPASSWORD -e PGSSLMODE=require postgres:18-alpine \
            psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -Atc "$1"
    fi
}

if [ "${2:-}" = "--sobre-la-real" ]; then
    echo "ATENCIÓN: se reemplazarán los datos de '$DB_NAME' ($AMBIENTE) con $ARCHIVO."
    read -r -p "Escribe RESTAURAR para continuar: " ok
    [ "$ok" = "RESTAURAR" ] || { echo "Cancelado"; exit 1; }
    if [ "$AMBIENTE" = "on-premise" ]; then
        $DOCKER compose exec -T -e PGPASSWORD db pg_restore -U "$DB_USER" -d "$DB_NAME" --clean --if-exists --no-owner < "$ARCHIVO"
    else
        $DOCKER run --rm -i -e PGPASSWORD -e PGSSLMODE=require postgres:18-alpine \
            pg_restore -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" --clean --if-exists --no-owner < "$ARCHIVO"
    fi
    echo "Restaurado. Inicia las réplicas: docker compose start backend1 backend2"
    exit 0
fi

PRUEBA=petadopt-restauracion
CLAVE_PRUEBA="prueba-$(date +%s)"
trap '$DOCKER rm -f $PRUEBA >/dev/null 2>&1 || true' EXIT

echo "1) Creando PostgreSQL temporal de prueba ($PRUEBA)..."
$DOCKER rm -f $PRUEBA >/dev/null 2>&1 || true
$DOCKER run -d --name $PRUEBA -e POSTGRES_PASSWORD="$CLAVE_PRUEBA" -e POSTGRES_DB=restaurada postgres:18-alpine >/dev/null
until $DOCKER exec $PRUEBA pg_isready -U postgres -d restaurada >/dev/null 2>&1; do sleep 1; done
sleep 2

echo "2) Restaurando $ARCHIVO..."
inicio=$(date +%s)
$DOCKER exec -i $PRUEBA pg_restore -U postgres -d restaurada --no-owner < "$ARCHIVO"
echo "   Restaurado en $(( $(date +%s) - inicio )) s"

echo "3) Comparando filas por tabla (respaldo vs base real actual):"
printf '   %-22s %10s %10s\n' TABLA RESPALDO REAL
for t in $TABLAS; do
    r=$($DOCKER exec $PRUEBA psql -U postgres -d restaurada -Atc "select count(*) from $t" 2>/dev/null || echo "-")
    a=$(consulta_real "select count(*) from $t" 2>/dev/null || echo "-")
    printf '   %-22s %10s %10s\n' "$t" "$r" "$a"
done
echo "   (Si hubo cambios después del respaldo, las cifras pueden diferir: es lo esperado.)"
echo "4) Eliminando el contenedor de prueba. Restauración verificada."
