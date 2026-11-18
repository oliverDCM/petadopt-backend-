# PetAdopt — Despliegue on-premise y cloud con NGINX balanceador (Sistemas Operativos)

Guía paso a paso del proyecto integrador híbrido aplicada a PetAdopt. Cada paso indica **qué captura tomar** (📸).
Lo que la guía hace con páginas HTML de ejemplo (APP1/APP2) aquí se hace con el proyecto real, como exige el anexo.

---

## 0. Arquitectura

```
                 Navegador (Windows)  /  curl
                          │  http://IP:8080   ← ÚNICO puerto publicado
                          ▼
            ┌──────────────────────────────┐
            │ balanceador  (nginx:1.27)    │  /api/*  → upstream api_backend (round robin)
            └──────┬──────────────┬────────┘  /       → frontend
                   │              │
        ┌──────────▼───┐   ┌──────▼───────┐   ┌──────────────┐
        │ backend1     │   │ backend2     │   │ frontend     │  Angular servido por nginx
        │ Spring Boot  │   │ Spring Boot  │   │ :8080 interno│
        │ :8080 interno│   │ :8080 interno│   └──────────────┘
        └──────┬───────┘   └──────┬───────┘
               └───────┬──────────┘          red Docker: petadopt-net
                       ▼
     On-premise: contenedor db (postgres:15) + volumen pgdata   (sin puerto público)
     Cloud:      Amazon RDS PostgreSQL (privada, solo acepta a la EC2, SSL)
     Fotos (ambos): Amazon S3            Correos de prueba (ambos): mailpit, solo 127.0.0.1:8025
```

| Componente | Imagen | Puerto | ¿Visible desde la red? |
|---|---|---|---|
| balanceador | `nginx:1.27-alpine` + `balanceador/nginx.conf` | host `PUERTO_APP` (8080) → 80 | **Sí** (único) |
| backend1, backend2 | `petadopt-backend:VERSION` (Dockerfile propio) | 8080 interno | No |
| frontend | `petadopt-frontend:VERSION` (Dockerfile propio) | 8080 interno | No |
| db (solo on-premise) | `postgres:15` + volumen `pgdata` | 127.0.0.1:5432 | No |
| mailpit | `axllent/mailpit` | 127.0.0.1:8025 | No |

**Mismo `docker-compose.yml` en los dos ambientes.** Solo cambia el `.env`: `.env.example` (on-premise) o `.env.cloud.example` (cloud).

Cómo se sabe qué réplica respondió: cada backend añade la cabecera **`X-Instancia: backend1|backend2`** y el balanceador añade **`X-Upstream`** (IP:puerto de la réplica). El log del balanceador (`logs/nginx/access.log`) registra `replica=` y el tiempo de cada petición. Cada réplica escribe su propio log: `logs/backend/backend1.log` y `backend2.log`.

---

## 1. Antes de empezar: subir los cambios (en tu PC, PowerShell)

En la carpeta `petadopt` (repo del backend):

```powershell
git rm -r --cached backend/deploy     # el despliegue con Caddy/GHCR se reemplaza por esta guía
Remove-Item -Recurse -Force backend/deploy
git add -A
git update-index --chmod=+x scripts/comun.sh scripts/monitor.sh scripts/probar-balanceo.sh scripts/respaldo-bd.sh scripts/restaurar-bd.sh
git status                            # revisa que NO aparezca .env, *.pem, respaldos/ ni reportes/
git commit -m "Balanceador NGINX con dos réplicas del backend, scripts de operación y guía SO"
git push origin HEAD:develop
```

Tu `.env` del PC necesita **dos líneas nuevas** (sin ellas no arranca la base de datos):

```
COMPOSE_PROFILES=db-local,herramientas
DB_PASSWORD=la-misma-que-tienes-en-POSTGRES_PASSWORD
```

`POSTGRES_DB` y `POSTGRES_USER` pueden quedarse como están: el compose los sigue aceptando.
Luego: `docker compose up -d --build --remove-orphans` (`--remove-orphans` borra el contenedor viejo `petadopt-backend`).
La app sigue en `http://localhost:4200`, ahora a través del balanceador.

---

## 2. Ambiente ON-PREMISE (VirtualBox + Ubuntu Server)

### Fase 1 — Máquina virtual e inventario

1. Descarga **Ubuntu Server 26.04 LTS** (la misma versión de la EC2: así la comparación es justa).
2. VirtualBox → Nueva: nombre `petadopt-onprem`, **2 CPU, 4096 MB RAM, disco 25 GB**.
   Red: **Adaptador puente** (la VM recibe una IP de tu red y la ves desde Windows).
   Si en tu red no funciona el puente: NAT + reenvío de puertos `8080→8080` y `2222→22`.
3. Instala Ubuntu: usuario `petadopt`, marca **Install OpenSSH server**. No instales Docker desde el instalador (snap).
4. Inventario 📸 (tabla CPU, RAM, disco, IP):

```bash
hostnamectl
lscpu | grep -E 'Model name|^CPU\(s\)'
free -h
df -h /
ip -4 addr show | grep inet
ping -c 3 8.8.8.8          # red
ping -c 3 github.com       # DNS
```

Desde Windows ya puedes entrar por SSH: `ssh petadopt@IP_DE_LA_VM` (más cómodo para copiar y pegar).

### Fase 2 — Docker y firewall

```bash
sudo apt update && sudo apt -y upgrade
sudo apt -y install docker.io docker-compose-v2 git curl
sudo systemctl enable --now docker
sudo usermod -aG docker $USER      # cierra la sesión SSH y vuelve a entrar
docker --version && docker compose version     # 📸

# Firewall del sistema operativo: solo SSH y el puerto del balanceador
sudo ufw allow OpenSSH
sudo ufw allow 8080/tcp
sudo ufw enable
sudo ufw status verbose            # 📸
```

> Docker publica puertos con sus propias reglas de iptables, que pasan **por delante** de ufw. Por eso el compose
> publica las herramientas solo en `127.0.0.1` y las réplicas no publican nada: no dependemos de ufw para protegerlas.

Medición **antes** del despliegue (para comparar en Fase 2, paso 5) 📸: `free -h; uptime; ps aux --sort=-%mem | head -6`

### Fase 3/4 — Obtener el proyecto y levantarlo

```bash
cd ~
git clone -b develop https://github.com/<tu-usuario>/<repo-backend>.git petadopt
cd petadopt
git clone -b develop https://github.com/oliverDCM/petadot-frontend.git petadot-frontend-main
# Si un repo es privado, en "Password" pega un token de GitHub (Settings → Developer settings →
# Fine-grained token, solo lectura de contenido). Nunca lo escribas en el comando.

cp .env.example .env
nano .env          # PUBLIC_URL=http://IP_DE_LA_VM:8080, DB_PASSWORD, JWT_SECRET (openssl rand -base64 48), S3_*
chmod 600 .env

docker compose build                 # 📸 (tarda: compila Maven y Angular)
docker compose up -d
docker compose ps                    # 📸 balanceador, backend1, backend2, frontend, db, mailpit — healthy
```

Desde el navegador de Windows: `http://IP_DE_LA_VM:8080` 📸

**Versiones v1 / v2 y rollback (Fase 4):**

```bash
docker images | grep petadopt                             # 📸 petadopt-backend:v1 y su IMAGE ID (hash)
docker image inspect --format '{{.Id}}' petadopt-backend:v1
# Haz un cambio visible (p. ej. un texto del frontend), cambia VERSION=v2 en .env y:
docker compose up -d --build                               # 📸 ahora corren las imágenes :v2
# Rollback: VERSION=v1 en .env y
docker compose up -d                                       # vuelve a v1 sin recompilar
```

### Caso de uso de extremo a extremo + persistencia (anexo, pasos 6 y 7)

1. En el navegador: regístrate, inicia sesión y **publica un animal con foto** 📸.
2. Verifica el dato en la base 📸:
   ```bash
   docker compose exec db psql -U petadopt -d petadopt -c "select id, nombre, especie, estado from animales order by id desc limit 3;"
   ```
3. Reinicia todo y comprueba que sigue ahí 📸:
   ```bash
   docker compose down        # SIN -v: -v borraría el volumen con los datos
   docker compose up -d
   docker volume ls | grep pgdata
   ```
   Recarga el catálogo: el animal sigue publicado (la base vive en el volumen `pgdata`; la foto, en S3).

### Balanceo y falla de una réplica (Fase 6 y anexo, pasos 9 y 10)

```bash
chmod +x scripts/*.sh
./scripts/probar-balanceo.sh            # 📸 10 peticiones alternando backend1/backend2
./scripts/probar-balanceo.sh --falla    # 📸 detiene backend1, mide, lo recupera y mide el tiempo de restauración
tail -20 logs/nginx/access.log          # 📸 columna replica=
```

Resultado esperado con backend1 detenido: todas las peticiones responden **200 desde backend2**. Si la primera
petición iba a backend1, el balanceador la reintenta solo en backend2 (`proxy_next_upstream`); en el log se ve como
`replica=172.x.x.x:8080, 172.x.x.y:8080`. Las peticiones POST/PUT no se reintentan (no son idempotentes): si una
llega justo cuando cae la réplica, puede dar un 502 transitorio — regístralo si aparece.

### Monitoreo (Fase 3 y anexo, paso 15)

```bash
./scripts/monitor.sh       # 📸 imprime y guarda reportes/monitor_<host>_<fecha>.log
```

Incluye fecha, hostname, IP, SO, uptime/carga, CPU, memoria, disco, procesos, contenedores (incluso detenidos),
`docker stats`, salud de cada réplica y de la base, códigos HTTP de `/`, `/api/animales` y `/salud`, reparto de 10
peticiones y últimos errores de los logs. Termina con `TODO OPERATIVO` o el número de comprobaciones fallidas.

### Respaldo y restauración (anexo, paso 17)

```bash
./scripts/respaldo-bd.sh                                   # 📸 respaldos/petadopt_on-premise_<fecha>.dump + SHA-256
./scripts/restaurar-bd.sh respaldos/petadopt_on-premise_<fecha>.dump   # 📸
```

`restaurar-bd.sh` verifica la huella, restaura en un PostgreSQL **temporal de prueba** (no toca la base real),
compara las filas por tabla con la base real y borra el contenedor temporal.
Recuperación real ante desastre: `docker compose stop backend1 backend2` y
`./scripts/restaurar-bd.sh <archivo> --sobre-la-real` (pide escribir RESTAURAR).

Diferencia para el informe: **respaldo de archivos** (código, compose, `.env` → Git y copia cifrada del `.env`) vs
**respaldo de datos persistentes** (el volumen `pgdata` → `pg_dump`). Copiar la carpeta del volumen en caliente
puede dar una copia inconsistente; `pg_dump` hace una foto consistente.

---

## 3. Ambiente CLOUD (AWS EC2 + RDS)

Recursos que ya existen: EC2 `petadopt` (Ohio, IP elástica, Ubuntu 26.04, Docker), RDS `petadopt-db`, bucket S3.

### Reglas de red 📸 (EC2 → Security groups)

**Grupo de la EC2 — reglas de entrada (solo estas):**

| Tipo | Puerto | Origen |
|---|---|---|
| SSH | 22 | **Mi IP** |
| TCP personalizado | 8080 | **Mi IP** (y la IP del docente el día de la sustentación) |

Borra las reglas de 80 y 443 si quedaron del despliegue anterior con Caddy.

**Grupo de RDS:** entrada PostgreSQL 5432 **solo desde el grupo de seguridad de la EC2** (no desde 0.0.0.0/0).
RDS → Conectividad → *Acceso público: No* 📸.

### Preparar el servidor

```bash
ssh -i petadopt.pem ubuntu@IP_ELASTICA

# 2 GB de swap: compilar Maven + Angular en una t3.small (2 GB RAM) sin quedarse sin memoria
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Guardar el .env del intento anterior y empezar limpio
mv ~/petadopt ~/petadopt-anterior 2>/dev/null
docker ps -a                       # si queda algún contenedor del intento anterior: docker rm -f <nombre>
```

### Obtener el proyecto y levantarlo

```bash
git clone -b develop https://github.com/<tu-usuario>/<repo-backend>.git ~/petadopt
cd ~/petadopt
git clone -b develop https://github.com/oliverDCM/petadot-frontend.git petadot-frontend-main
git log -1 --format='%h %s'        # 📸 mismo commit que on-premise = misma versión

cp .env.cloud.example .env
nano .env     # PUBLIC_URL=http://IP_ELASTICA:8080, DB_HOST (endpoint RDS), DB_PASSWORD, JWT_SECRET, S3_*
              # Los valores de RDS y S3 están en ~/petadopt-anterior/.env
chmod 600 .env

time docker compose up -d --build  # 📸 el "real" sirve como tiempo de despliegue para la comparación
docker compose ps                  # 📸 sin contenedor db: la base es RDS
curl -s localhost:8080/salud       # {"status":"UP"...}
```

Navegador de Windows: `http://IP_ELASTICA:8080` 📸 (registrarse, publicar un animal).
Luego, igual que on-premise: `./scripts/probar-balanceo.sh --falla`, `./scripts/monitor.sh`,
`./scripts/respaldo-bd.sh` y `./scripts/restaurar-bd.sh <archivo>` (en cloud el respaldo se hace contra RDS con un
contenedor temporal de PostgreSQL; RDS además tiene sus propias copias automáticas) 📸.

> El `.env` se escribe **en el servidor**: no se copia por SCP ni se sube a Git. Si la guía pide evidencia de SCP,
> transfiere un archivo no sensible, p. ej. desde Windows: `scp -i petadopt.pem .\docs\DESPLIEGUE-SO.md ubuntu@IP_ELASTICA:~/` 📸

---

## 4. Incidentes (tickets)

Plantilla para cada uno: **ID · fecha/hora · síntoma · impacto · diagnóstico (comandos y salida) · causa raíz ·
acción correctiva · evidencia de recuperación (tiempo) · limitaciones / prevención.**

**INC-001 — Réplica backend1 detenida** (on-premise y cloud)
- Provocar: `docker compose stop backend1` · Detectar: `./scripts/monitor.sh` → `backend1: CAÍDA`; `docker compose ps -a`
- Diagnóstico: `docker compose logs --tail=30 backend1`, `docker stats --no-stream`, `tail logs/nginx/error.log` (connect() failed)
- Impacto: ninguno visible para el usuario (backend2 atiende todo); capacidad a la mitad.
- Recuperación: `docker compose start backend1` · Tiempo: lo da `./scripts/probar-balanceo.sh --falla`.

**INC-002 — El puerto 8080 ya está ocupado** (on-premise)
- Provocar: `docker compose down` y en otra terminal `python3 -m http.server 8080`; luego `docker compose up -d`
  → error *address already in use*.
- Diagnóstico: `sudo ss -ltnp | grep :8080` (muestra el proceso python3 y su PID), `docker ps`.
- Corrección: detener el proceso que lo ocupa (Ctrl+C / `kill <PID>`) **o** usar otro puerto: `PUERTO_APP=8081` y
  `PUBLIC_URL=...:8081` en `.env`, `sudo ufw allow 8081/tcp`, `docker compose up -d`. Validar con `curl -I localhost:8081`.

**INC-003 — Funciona en la EC2 pero no desde internet** (cloud)
- Provocar: quitar la regla 8080 del grupo de seguridad.
- Diagnóstico por capas:
  1. Publicación del puerto: `docker compose ps` (debe decir `0.0.0.0:8080->80/tcp`) y `curl -I localhost:8080` en la EC2 → 200.
  2. Firewall del SO: `sudo ufw status` (en la EC2 suele estar inactivo).
  3. Regla de seguridad cloud: desde Windows `Test-NetConnection IP_ELASTICA -Port 8080` → `TcpTestSucceeded: False`.
- Causa: falta la regla de entrada del grupo de seguridad. Corrección: volver a crearla solo para tu IP.

---

## 5. Comparación on-premise vs cloud (anexo, paso 14)

Mide desde Windows la latencia de cada ambiente (10 peticiones):

```powershell
1..10 | % { (Measure-Command { Invoke-WebRequest http://IP:8080/api/animales -UseBasicParsing }).TotalMilliseconds }
```

| Aspecto | On-premise (VirtualBox) | Cloud (AWS) |
|---|---|---|
| IP y red | IP privada de la LAN (puente) | IP elástica pública, VPC, grupos de seguridad |
| CPU / RAM | 2 vCPU / 4 GB (del PC anfitrión) | t3.small: 2 vCPU / 2 GB (+2 GB swap) |
| Almacenamiento | disco virtual 25 GB | EBS gp3 + RDS 20 GB |
| Base de datos | contenedor + volumen `pgdata` | RDS administrada, copias automáticas |
| Tiempo de despliegue | (medir con `time docker compose up -d --build`) | (medir) |
| Latencia observada | (medir) | (medir) |
| Estado HTTP / versión | 200 · commit `git log -1` | 200 · mismo commit |
| Costo estimado | electricidad del PC | EC2 t3.small ≈ US$0,02/h + RDS db.t4g.micro ≈ US$0,016/h + IP elástica (verificar en la calculadora de AWS / capa gratuita) |
| Riesgos | se apaga el PC = se cae todo | costos si se olvida encendida; exposición a internet |
| Recuperación | `restaurar-bd.sh` desde un .dump | snapshots de RDS + `restaurar-bd.sh` |

---

## 6. Varias réplicas: qué cambió y qué no (consideración técnica del anexo)

- **Sesiones:** JWT sin estado → cualquier réplica valida el token. No se necesitan sesiones pegajosas.
- **Archivos:** las fotos van a S3, no al disco del contenedor → las dos réplicas ven las mismas fotos.
- **Base de datos:** una sola, compartida. No se duplica (no hay estrategia de replicación).
- **Límites en memoria** (intentos de login, recuperación de contraseña): cada réplica lleva su propia cuenta, así que
  el límite efectivo es hasta el doble. El balanceador añade un límite global por IP (`limit_req`). Mejora futura:
  guardar esos contadores en Redis o en la base.
- **Tarea programada de limpieza de tokens:** corre en las dos réplicas; es idempotente (borrar lo vencido dos veces no
  causa errores). Mejora futura: bloqueo distribuido (ShedLock).
- **Logs:** un archivo por réplica (`backend1.log`, `backend2.log`).
- **Límite:** las dos réplicas están en la misma VM → si cae la VM o el balanceador, cae todo (punto único de falla).

## 7. Cierre y costos

- **No borres** la EC2 ni RDS antes de la sustentación. Mientras no la uses: EC2 → *Detener instancia* y RDS →
  *Detener temporalmente* (RDS se vuelve a encender sola a los 7 días).
- Después de la nota: borrar EC2, RDS (sin snapshot final si no lo necesitas), liberar la IP elástica y vaciar/borrar el
  bucket; captura de la consola sin recursos 📸.
- On-premise: `docker compose down` (sin `-v` si quieres conservar datos).
