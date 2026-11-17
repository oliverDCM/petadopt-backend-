# Pruebas de carga y estrés de PetAdopt (Apache JMeter)

## Archivos

| Archivo | Para qué |
|---|---|
| `petadopt-carga-estres.jmx` | Plan de JMeter con los 9 escenarios obligatorios (E1 a E9) y la preparación de datos (E0) |
| `ejecutar-pruebas.ps1` | Corre todos los escenarios en orden, genera los reportes HTML y mide CPU y memoria de los contenedores |

## Escenarios

| # | Tipo | Escenario | Endpoints de PetAdopt | Usuarios | Ramp-up | Loops / duración |
|---|---|---|---|---|---|---|
| E0 | — | Preparar datos | Registra 50 adoptantes y 50 vendedores; cada vendedor publica 1 animal | 1 | — | 50 |
| E1 | Carga | Login concurrente | `POST /api/auth/login` | 30 | 10 s | 5 |
| E2 | Carga | Consulta masiva | `GET /api/animales`, `?especie=PERRO`, `/api/animales/{id}` | 50 | 15 s | 10 |
| E3 | Carga | Registro concurrente | `POST /api/auth/register` (email único y luego el mismo email, que debe dar 409 = sin duplicados) | 30 | 10 s | 3 |
| E4 | Carga | Flujo completo | login → catálogo → publicar → actualizar → consulta final | 15 | 10 s | 5 |
| E5 | Estrés | Saturación de login | `POST /api/auth/login` en 4 fases | 50 → 100 → 200 → 300 | 10 s | 5 |
| E6 | Estrés | Sobrecarga de consultas | catálogo, filtro y detalle | 200 | 20 s | 5 |
| E7 | Estrés | Sobrecarga CRUD | crear → actualizar → retirar (`PUT /estado` RETIRADO) | 100 | 10 s | 20 |
| E8 | Estrés | Resistencia continua | login + consultas con pausas de 0,5 a 1,5 s | 50 | 30 s | 10 min |
| E9 | Estrés | Falla controlada | login + consultas sin pausas, hasta provocar errores; después mide cuánto tarda en recuperarse | 500 | 60 s | 3 min |

Cada petición tiene aserciones: código HTTP esperado y campos del JSON (token, id, descripción actualizada, estado RETIRADO). Una respuesta 200 con datos incorrectos también cuenta como error.

> PetAdopt no tiene un endpoint para borrar animales: en E7 la operación "eliminar" es **retirar** la publicación (cambio de estado), que es como se da de baja un animal en el sistema.

## Cómo ejecutarlas

1. **Requisitos:** Java 17 (ya lo tienes) y JMeter 5.6.3. Descarga el `.zip` "Binaries" de https://jmeter.apache.org/download_jmeter.cgi y descomprímelo en `C:\apache-jmeter-5.6.3`.
2. Levanta el sistema: en la carpeta `petadopt`, ejecuta `docker compose up -d` y espera a que `backend` esté *healthy*.
3. Copia esta carpeta en `petadopt\pruebas-carga` y, en PowerShell dentro de ella, ejecuta:
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
   ```
   Dura unos **25 a 30 minutos** (E8 son 10 minutos y E9 son 3). Para correr solo algunos: `-Solo 1,2`. Si JMeter está en otra ruta: `-JMeter "D:\jmeter\bin\jmeter.bat"`.
4. Al terminar, cada escenario tiene su reporte en `resultados\reportes\<escenario>\index.html`.

Mientras corren las pruebas, no uses la app ni cierres Docker: los resultados deben reflejar solo la carga de JMeter.

## Evidencias para el documento (sección 20.6)

- **Plan:** abre `jmeter.bat`, luego `File → Open → petadopt-carga-estres.jmx` y toma una captura del árbol de escenarios y de un grupo de hilos con su configuración.
- **Corrida en la interfaz:** en JMeter, haz clic en ▶. Por defecto corre E1. Toma capturas de *Resumen (Summary Report)*, *Informe agregado* y *Gráfico de tiempos de respuesta*. (Para cargas grandes se usa el modo consola, como hace el script, porque la interfaz consume mucha memoria.)
- **Reportes HTML:** captura de `index.html` (APDEX y tabla *Statistics*) y de *Charts → Response Times Over Time* de E5 (las 4 fases), E8 y E9.
- **Recursos:** `docker stats` durante E9, o los `.csv` de `resultados\recursos`.

## Para el análisis técnico

Comprime la carpeta **`resultados`** completa y envíala. Con esos números se escriben los resultados (20.5), el análisis (20.7) y las propuestas de mejora (20.8): cuándo empieza a degradarse, qué endpoint es el más lento, el impacto de la base de datos y la recuperación después de E9.
