<#
  Ejecuta los escenarios de carga y estrés de PetAdopt con JMeter (modo consola, como recomienda JMeter).
  Por cada escenario guarda:
    resultados\jtl\eN.jtl          muestras crudas
    resultados\reportes\eN\        reporte HTML (abrir index.html)
    resultados\recursos\eN.csv     CPU y memoria de los contenedores cada 5 s (docker stats)

  Uso (PowerShell, en esta carpeta, con docker compose levantado):
    powershell -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
    powershell -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1 -Solo 1,2      # solo algunos
#>
param(
  [string]$JMeter = "C:\apache-jmeter-5.6.3\bin\jmeter.bat",
  [string]$Servidor = "localhost",
  [int]$Puerto = 8080,
  [int[]]$Solo = @()
)
$ErrorActionPreference = "Stop"
$plan = Join-Path $PSScriptRoot "petadopt-carga-estres.jmx"
$salida = Join-Path $PSScriptRoot "resultados"
New-Item -ItemType Directory -Force "$salida\jtl", "$salida\reportes", "$salida\recursos" | Out-Null

if (-not (Test-Path $JMeter)) { throw "No encuentro JMeter en $JMeter. Usa -JMeter 'ruta\a\bin\jmeter.bat'" }
try { Invoke-RestMethod "http://${Servidor}:${Puerto}/actuator/health" | Out-Null }
catch { throw "El backend no responde en http://${Servidor}:${Puerto}/actuator/health. Levanta docker compose primero." }

# Escenario, nombre, propiedades -J (usuarios / rampup / loops / duracion en segundos)
$escenarios = @(
  @{ n=0; id="e0-preparacion";   p=@{ cuentas=50 } },
  @{ n=1; id="e1-login";         p=@{ usuarios=30;  rampup=10; loops=5 } },
  @{ n=2; id="e2-consultas";     p=@{ usuarios=50;  rampup=15; loops=10 } },
  @{ n=3; id="e3-registro";      p=@{ usuarios=30;  rampup=10; loops=3 } },
  @{ n=4; id="e4-flujo";         p=@{ usuarios=15;  rampup=10; loops=5 } },
  @{ n=5; id="e5-login-050";     p=@{ usuarios=50;  rampup=10; loops=5 } },
  @{ n=5; id="e5-login-100";     p=@{ usuarios=100; rampup=10; loops=5 } },
  @{ n=5; id="e5-login-200";     p=@{ usuarios=200; rampup=10; loops=5 } },
  @{ n=5; id="e5-login-300";     p=@{ usuarios=300; rampup=10; loops=5 } },
  @{ n=6; id="e6-sobrecarga";    p=@{ usuarios=200; rampup=20; loops=5 } },
  @{ n=7; id="e7-crud";          p=@{ usuarios=100; rampup=10; loops=20 } },
  @{ n=8; id="e8-resistencia";   p=@{ usuarios=50;  rampup=30; duracion=600 } },
  @{ n=9; id="e9-falla";         p=@{ usuarios=500; rampup=60; duracion=180 } }
)
if ($Solo.Count -gt 0) { $escenarios = $escenarios | Where-Object { $_.n -eq 0 -or $Solo -contains $_.n } }

function Medir-Recursos([string]$archivo) {
  Start-Job -ArgumentList $archivo -ScriptBlock {
    param($f)
    "hora,contenedor,cpu,memoria" | Set-Content $f
    while ($true) {
      docker stats --no-stream --format "{{.Name}},{{.CPUPerc}},{{.MemUsage}}" |
        ForEach-Object { "$(Get-Date -Format HH:mm:ss),$_" } | Add-Content $f
      Start-Sleep -Seconds 5
    }
  }
}

function Correr([hashtable]$e) {
  $id = $e.id
  Write-Host "`n=== $id ===" -ForegroundColor Cyan
  $jtl = "$salida\jtl\$id.jtl"; $rep = "$salida\reportes\$id"
  Remove-Item $jtl, $rep -Recurse -Force -ErrorAction SilentlyContinue
  $jargs = @("-n", "-t", $plan, "-l", $jtl, "-e", "-o", $rep,
            "-Jescenario=$($e.n)", "-Jhost=$Servidor", "-Jpuerto=$Puerto")
  foreach ($k in $e.p.Keys) { $jargs += "-J$k=$($e.p[$k])" }
  $job = Medir-Recursos "$salida\recursos\$id.csv"
  $inicio = Get-Date
  & $JMeter @jargs
  Stop-Job $job; Remove-Job $job
  Write-Host ("Terminó en {0:N0} s. Reporte: {1}\index.html" -f ((Get-Date) - $inicio).TotalSeconds, $rep) -ForegroundColor Green
}

foreach ($e in $escenarios) {
  Correr $e
  if ($e.n -eq 9) {
    # Recuperación después de la falla controlada: cuánto tarda el backend en volver a estar sano
    Write-Host "Midiendo recuperación del backend..." -ForegroundColor Yellow
    $t0 = Get-Date; $estado = "DOWN"
    while (((Get-Date) - $t0).TotalSeconds -lt 180) {
      try { $estado = (Invoke-RestMethod "http://${Servidor}:${Puerto}/actuator/health" -TimeoutSec 5).status } catch { $estado = "DOWN" }
      if ($estado -eq "UP") { break }
      Start-Sleep -Seconds 2
    }
    $seg = [int]((Get-Date) - $t0).TotalSeconds
    "estado=$estado; segundos_hasta_UP=$seg" | Set-Content "$salida\recuperacion-e9.txt"
    Write-Host "Backend $estado después de $seg s" -ForegroundColor Yellow
    Correr @{ n=2; id="e9-recuperacion"; p=@{ usuarios=10; rampup=5; loops=2 } }
  }
  Start-Sleep -Seconds 15   # pausa entre escenarios para que el sistema se estabilice
}
docker compose ps | Out-File "$salida\docker-ps-final.txt"
Write-Host "`nListo. Comprime la carpeta 'resultados' y envíala para el análisis." -ForegroundColor Green
