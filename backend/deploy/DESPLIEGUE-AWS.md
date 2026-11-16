# Despliegue de PetAdopt en AWS (EC2 + RDS + S3) con GitHub Actions

```
                         ┌──────────── AWS (Ohio, us-east-2) ─────────────┐
 Navegador ──HTTP:80──►  │  EC2 (Docker)                                   │
                         │   ├─ frontend (nginx) ──/api──► backend ────────┼──► RDS PostgreSQL (privada)
                         │   └─ mailpit                       │            │
                         │                                    └────────────┼──► S3 (fotos)
                         └─────────────────────────────────────────────────┘
 GitHub Actions ──SSH──► EC2: docker compose pull + up   (imágenes en GHCR)
```

| Pieza | Servicio AWS | Por qué |
|---|---|---|
| Frontend + backend | **EC2** con Docker | Corre las imágenes que publica el pipeline |
| Base de datos | **RDS PostgreSQL** | Administrada: copias de seguridad automáticas, parches, no se pierde si se borra el servidor |
| Fotos | **S3** | Ya configurado (`petadopt-fotos-oliver`) |

Todo en la región **Ohio (`us-east-2`)**, la misma del bucket. Revisa la región arriba a la derecha de la consola antes de cada paso.

---

## Plan de hoy (en este orden)

1. Aplicar los zips en tus repos (logs, E2E, pipelines y esta carpeta `deploy/`).
2. Crear el servidor **EC2**.
3. Crear la base de datos **RDS** conectada a EC2.
4. Preparar el servidor (Docker y `.env`).
5. Configurar **SonarCloud** y los **secretos de GitHub**.
6. Hacer push a `main`: primero el backend y después el frontend.
7. Probar en `http://<IP>`.

---

## 1. Crear el servidor EC2

**EC2 → Lanzar instancia**:

| Campo | Valor |
|---|---|
| Nombre | `petadopt` |
| Imagen (AMI) | **Ubuntu Server 24.04 LTS** |
| Tipo | **t3.small** (2 GB). Usa créditos del plan gratis; `t3.micro` también sirve, pero va justo de memoria |
| Par de claves | **Crear nuevo par de claves** → `petadopt`, RSA, `.pem` → se descarga `petadopt.pem`. Guárdalo bien y **nunca** lo subas a GitHub |
| Red | Marca **Permitir tráfico SSH desde: Cualquier lugar** y **Permitir tráfico HTTP desde Internet** |
| Almacenamiento | **20 GiB gp3** |

Luego haz clic en **Lanzar instancia**.

**IP fija:** EC2 → **Direcciones IP elásticas** → **Asignar** → selecciónala → **Acciones → Asociar** → elige la instancia `petadopt`. Anota la IP.

> SSH queda abierto porque GitHub Actions se conecta desde IPs que cambian. El servidor solo acepta la llave `.pem`, no contraseñas.

## 2. Crear la base de datos RDS

**RDS → Bases de datos → Crear base de datos**:

| Campo | Valor |
|---|---|
| Método | **Creación estándar** |
| Motor | **PostgreSQL** (la versión que venga por defecto) |
| Plantilla | **Capa gratuita** (*Free tier*) |
| Identificador | `petadopt-db` |
| Nombre de usuario maestro | `petadopt` |
| Administración de credenciales | **Autoadministrado** → escribe una contraseña fuerte y guárdala (será `DB_PASSWORD`) |
| Clase de instancia | `db.t4g.micro` o `db.t3.micro` |
| Almacenamiento | 20 GiB gp3; **desmarca** "Habilitar escalado automático de almacenamiento" |
| Conectividad | **Conectarse a un recurso informático de EC2** → elige `petadopt`. AWS crea solo las reglas para que **solo** EC2 pueda entrar a la base |
| Acceso público | **No** (la base no queda expuesta a internet) |
| **Configuración adicional → Nombre de base de datos inicial** | `petadopt` ← **importante**: si lo dejas vacío, la base no se crea |
| Copias de seguridad | Deja las automáticas (1 a 7 días) |

Haz clic en **Crear base de datos**. Tarda unos 10 minutos. Cuando diga **Disponible**, entra y copia el **Punto de enlace** (*Endpoint*), algo como `petadopt-db.abc123xyz.us-east-2.rds.amazonaws.com`. Ese será `DB_HOST`.

> Las tablas las crea el backend solo al arrancar (`JPA_DDL_AUTO=update`).

## 3. Preparar el servidor

Desde PowerShell, en la carpeta donde está `petadopt.pem`:

```powershell
ssh -i petadopt.pem ubuntu@<IP_ELASTICA>
```

Si Windows reclama por los permisos del `.pem`: clic derecho → Propiedades → Seguridad → deja solo a tu usuario con lectura.

Ya dentro del servidor:

```bash
# Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu

# 2 GB de memoria swap (por si la RAM se llena)
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
exit
```

Vuelve a entrar con `ssh` para que tome el grupo `docker`. Luego:

```bash
# Comprobar que EC2 llega a RDS (pide la contraseña de la base)
docker run --rm -it postgres:16 psql "host=<ENDPOINT_RDS> user=petadopt dbname=petadopt sslmode=require" -c "select version();"

# Crear el .env del servidor
mkdir -p ~/petadopt/logs/backend && nano ~/petadopt/.env
```

En `nano`, pega `deploy/env.produccion.example` con tus valores: `DB_HOST`, `DB_PASSWORD`, `JWT_SECRET`, las `S3_*` y `PUBLIC_URL=http://<IP_ELASTICA>`. Guarda con Ctrl+O, Enter y Ctrl+X. Para generar el JWT usa `openssl rand -base64 48`.

## 4. SonarCloud

1. Entra a https://sonarcloud.io → **Log in with GitHub**.
2. Haz clic en **+** → **Create new organization** → elige tu cuenta → plan **Free** (los repos deben ser **públicos**).
3. Haz clic en **+** → **Analyze new project** → marca el repo del backend y el del frontend.
4. En cada proyecto ve a **Administration → Analysis Method** y **desactiva "Automatic Analysis"**.
5. En **Information** copia el **Organization Key** y el **Project Key** de cada proyecto.
6. Ve a **My Account → Security → Generate token** y copia el token.

## 5. Secretos de GitHub (en los **dos** repos)

Ve a **Settings → Secrets and variables → Actions**:

| Pestaña | Nombre | Valor |
|---|---|---|
| Secrets | `SONAR_TOKEN` | Token de SonarCloud |
| Secrets | `EC2_HOST` | IP elástica |
| Secrets | `EC2_USER` | `ubuntu` |
| Secrets | `EC2_SSH_KEY` | Todo el contenido de `petadopt.pem`, incluidas las líneas `-----BEGIN` y `-----END` |
| Variables | `SONAR_ORGANIZATION` | Organization Key |
| Variables | `SONAR_PROJECT_KEY` | Project Key **de ese repo** |

## 6. Primer despliegue

1. **Backend:** push a `main` → pestaña **Actions** → espera el ✅ de "Desplegar en AWS EC2".
2. **Frontend:** push a `main` → espera el ✅. El último paso prueba `http://<IP>/` y `/api/animales`.
3. Abre **`http://<IP_ELASTICA>/`**, regístrate y publica un animal con foto.

## 7. Operación

```bash
ssh -i petadopt.pem ubuntu@<IP>
cd ~/petadopt
docker compose -f docker-compose.prod.yml ps                # estado
docker compose -f docker-compose.prod.yml logs -f backend   # logs en vivo
tail -f ~/petadopt/logs/backend/petadopt.log                # logs guardados en archivo
```

- **Correos de prueba:** abre un túnel con `ssh -i petadopt.pem -L 8025:localhost:8025 ubuntu@<IP>` y entra a http://localhost:8025.
- **Volver a una versión anterior:** en `.env` pon `BACKEND_TAG=<sha del commit>` y ejecuta `docker compose -f docker-compose.prod.yml up -d backend`.
- **Base de datos:** RDS → `petadopt-db` → **Instantáneas** para hacer copias manuales antes de cambios grandes.
- **Para no gastar créditos** cuando no lo uses: detén EC2 (**Estado de la instancia → Detener**) y RDS (**Acciones → Detener temporalmente**; se vuelve a encender sola a los 7 días).

## Problemas comunes

| Síntoma | Causa | Solución |
|---|---|---|
| El backend no queda *healthy*; el log dice `Connection refused` o `timeout` a RDS | RDS no está conectada a EC2 | RDS → `petadopt-db` → **Acciones → Configurar conexión con EC2** → `petadopt` |
| `database "petadopt" does not exist` | No pusiste el nombre de base de datos inicial | Desde EC2: `docker run --rm -it postgres:16 psql "host=<ENDPOINT> user=petadopt dbname=postgres sslmode=require" -c "create database petadopt;"` |
| `password authentication failed` | `DB_PASSWORD` incorrecta | Corrige el `.env` y ejecuta `docker compose -f docker-compose.prod.yml up -d backend` |
| El pipeline falla en "Preparar la llave SSH" | `EC2_HOST` o `EC2_SSH_KEY` están mal | Revisa que la llave tenga las líneas BEGIN y END completas |
| Error 502 al subir fotos | S3 | Revisa las variables `S3_*` (`S3_REGION=us-east-2`) |
