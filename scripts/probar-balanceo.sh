#!/usr/bin/env bash
# probar-balanceo.sh — evidencia del reparto round robin y de la falla de una réplica (anexo, pasos 9 y 10;
# Fase 6, paso 7). Usa el endpoint público GET /api/animales y la cabecera X-Instancia que pone cada réplica.
#
#   ./scripts/probar-balanceo.sh            solo reparto: 10 peticiones
#   ./scripts/probar-balanceo.sh --falla    reparto, detiene backend1, mide, lo vuelve a iniciar y mide el
#                                           tiempo hasta que vuelve a recibir tráfico (para el ticket INC-001)
set -u
source "$(dirname "$0")/comun.sh"

N=10
URL="$URL_LOCAL/api/animales"

pedir() {   # imprime: código HTTP, réplica y tiempo de una petición
    curl -s -o /dev/null -D - -w 'TIEMPO %{time_total}\n' --max-time 5 "$URL" | tr -d '\r' \
      | awk 'NR==1{c=$2} tolower($1)=="x-instancia:"{r=$2} $1=="TIEMPO"{t=$2} END{printf "HTTP %s  %-9s %s s\n", (c?c:"000"), (r?r:"-"), t}'
}

ronda() {
    echo "== $1 ($(date +%T))"
    for i in $(seq 1 "$N"); do printf '  %2d  ' "$i"; pedir; done | tee /tmp/ronda.txt
    echo "  Resumen: backend1=$(grep -c backend1 /tmp/ronda.txt)  backend2=$(grep -c backend2 /tmp/ronda.txt)  errores=$(grep -vc 'HTTP 200' /tmp/ronda.txt)"
}

ronda "Reparto con las dos réplicas"

if [ "${1:-}" = "--falla" ]; then
    echo; echo "== Deteniendo backend1 ($(date +%T))"
    $DOCKER compose stop backend1
    ronda "Con backend1 detenido"
    echo; echo "== Iniciando backend1 de nuevo"
    inicio=$(date +%s)
    $DOCKER compose start backend1
    until [ "$($DOCKER inspect -f '{{.State.Health.Status}}' petadopt-backend1 2>/dev/null)" = "healthy" ]; do sleep 2; done
    echo "  backend1 sano a los $(( $(date +%s) - inicio )) s"
    # El balanceador lo vuelve a usar cuando vence fail_timeout (10 s)
    for _ in $(seq 1 60); do pedir | grep -q backend1 && break; sleep 1; done
    echo "  backend1 vuelve a recibir tráfico a los $(( $(date +%s) - inicio )) s (tiempo de restauración)"
    ronda "Después de la recuperación"
    echo; echo "Lo que vio el balanceador:"
    $DOCKER compose logs --tail=15 balanceador
fi
