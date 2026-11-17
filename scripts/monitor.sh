#!/usr/bin/env bash
# monitor.sh — instantánea del servidor y de PetAdopt (Fase 3 + Fase 6 + anexo, paso 15).
# Registra fecha, hostname, CPU, memoria, disco, procesos, contenedores, consumo Docker, salud de cada
# réplica y códigos HTTP del frontend y de la API a través del balanceador.
#
#   ./scripts/monitor.sh            imprime el reporte y lo guarda en reportes/monitor_<host>_<fecha>.log
#
# Es una foto del momento, no un sistema de alertas. Código de salida 0 si todo responde, 1 si algo falla.
set -u
source "$(dirname "$0")/comun.sh"

mkdir -p reportes
REPORTE="reportes/monitor_$(hostname)_$(date +%Y%m%d_%H%M%S).log"
FALLOS=0

{
echo "================ REPORTE PETADOPT ================"
echo "Fecha:      $(date -Is)"
echo "Servidor:   $(hostname)   Ambiente: $AMBIENTE"
echo "IP(s):      $(hostname -I 2>/dev/null)"
echo "Sistema:    $(. /etc/os-release 2>/dev/null; echo "${PRETTY_NAME:-desconocido}") / kernel $(uname -r)"

printf '\n--- DISPONIBILIDAD Y CARGA (uptime) ---\n'; uptime
printf '\n--- CPU ---\n'
echo "Núcleos: $(nproc)"
top -bn1 | grep -E '^(%Cpu|Cpu)' || true
printf '\n--- MEMORIA ---\n'; free -h
printf '\n--- DISCO ---\n'; df -h / | sed -n '1,2p'
printf '\n--- PROCESOS QUE MÁS MEMORIA USAN ---\n'; ps aux --sort=-%mem | head -6

printf '\n--- CONTENEDORES (incluye detenidos) ---\n'
$DOCKER compose ps -a --format 'table {{.Name}}\t{{.Status}}\t{{.Ports}}'
printf '\n--- CONSUMO DOCKER ---\n'
$DOCKER stats --no-stream --format 'table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}'

printf '\n--- SALUD DE CADA RÉPLICA (desde dentro de la red de Docker) ---\n'
for r in backend1 backend2; do
    if $DOCKER compose exec -T "$r" wget -q -O - http://127.0.0.1:8080/actuator/health 2>/dev/null | grep -q '"UP"'; then
        echo "$r: UP"
    else
        echo "$r: CAÍDA"; FALLOS=$((FALLOS + 1))
    fi
done

printf '\n--- BASE DE DATOS (%s) ---\n' "$AMBIENTE"
if [ "$AMBIENTE" = "on-premise" ]; then
    if $DOCKER compose exec -T db pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; then
        echo "PostgreSQL en contenedor: acepta conexiones"
    else
        echo "PostgreSQL en contenedor: NO responde"; FALLOS=$((FALLOS + 1))
    fi
else
    echo "Amazon RDS ($DB_HOST): la salud de las réplicas (arriba) incluye su conexión a la base"
fi

printf '\n--- HTTP A TRAVÉS DEL BALANCEADOR (%s) ---\n' "$URL_LOCAL"
for ruta in / /api/animales /salud; do
    medida="$(curl -s -o /dev/null -w '%{http_code} %{time_total}' --max-time 5 "$URL_LOCAL$ruta")"
    codigo="${medida%% *}"
    printf '%-15s HTTP %s  (%s s)\n' "$ruta" "$codigo" "${medida#* }"
    [ "$codigo" = "200" ] || FALLOS=$((FALLOS + 1))
done

printf '\n--- REPARTO DE 10 PETICIONES A LA API ---\n'
for i in $(seq 1 10); do
    curl -s -o /dev/null -D - --max-time 5 "$URL_LOCAL/api/animales" \
      | tr -d '\r' | awk -v n="$i" 'NR==1{c=$2} tolower($1)=="x-instancia:"{r=$2} END{printf "  %2d  HTTP %s  %s\n", n, c, (r?r:"(sin respuesta)")}'
done

printf '\n--- ÚLTIMOS ERRORES EN LOS LOGS ---\n'
for f in logs/backend/backend1.log logs/backend/backend2.log; do
    if [ -f "$f" ]; then
        echo "$f: $(grep -c ' ERROR ' "$f") líneas ERROR en total"
        grep ' ERROR ' "$f" | tail -n 2 | cut -c1-200
    fi
done
if [ -f logs/nginx/error.log ]; then
    echo "logs/nginx/error.log: últimas 3 líneas"; tail -n 3 logs/nginx/error.log | cut -c1-200
fi

printf '\n================ RESULTADO: '
if [ "$FALLOS" -eq 0 ]; then echo "TODO OPERATIVO ================"; else echo "$FALLOS COMPROBACIÓN(ES) FALLIDA(S) ================"; fi
} 2>&1 | tee "$REPORTE"

echo "Reporte guardado en: $REPORTE"
grep -q 'TODO OPERATIVO' "$REPORTE"
