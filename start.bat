@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION
CD /D "%~dp0"

WHERE docker >NUL 2>NUL
IF ERRORLEVEL 1 (
  ECHO Docker Desktop is not installed or docker is not in PATH.
  PAUSE
  EXIT /B 1
)

WHERE java >NUL 2>NUL
IF ERRORLEVEL 1 (
  ECHO Java 21 is not installed or java is not in PATH.
  PAUSE
  EXIT /B 1
)

docker info >NUL 2>NUL
IF ERRORLEVEL 1 (
  ECHO Starting Docker Desktop...
  IF EXIST "C:\Program Files\Docker\Docker\Docker Desktop.exe" (
    START "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
  ) ELSE (
    ECHO Please start Docker Desktop manually, then run this script again.
    PAUSE
    EXIT /B 1
  )

  FOR /L %%I IN (1,1,60) DO (
    docker info >NUL 2>NUL
    IF NOT ERRORLEVEL 1 GOTO docker_ready
    POWERSHELL -NoProfile -Command "Start-Sleep -Seconds 2"
  )
  ECHO Docker Desktop did not become ready within 120 seconds.
  PAUSE
  EXIT /B 1
)

:docker_ready
IF NOT EXIST "runtime\tourism-webgis.jar" (
  ECHO Application package not found. Running first build...
  CALL build.bat
  IF ERRORLEVEL 1 (
    PAUSE
    EXIT /B 1
  )
)

ECHO Starting PostGIS...
docker compose up -d postgis
IF ERRORLEVEL 1 (
  PAUSE
  EXIT /B 1
)

ECHO Waiting for PostGIS health check...
FOR /L %%I IN (1,1,40) DO (
  FOR /F "delims=" %%S IN ('docker inspect --format "{{.State.Health.Status}}" xian-tourism-postgis 2^>NUL') DO SET DB_STATUS=%%S
  IF "!DB_STATUS!"=="healthy" GOTO database_ready
  POWERSHELL -NoProfile -Command "Start-Sleep -Seconds 2"
)
ECHO PostGIS did not become healthy within 80 seconds.
docker compose logs postgis
PAUSE
EXIT /B 1

:database_ready
IF NOT EXIST runtime MKDIR runtime

POWERSHELL -NoProfile -Command "try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:8080/api/health' -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } } catch {}; exit 1"
IF NOT ERRORLEVEL 1 GOTO application_ready

ECHO Starting Spring Boot...
POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command ^
  "$p = Start-Process -FilePath 'java' -ArgumentList '-jar','runtime\tourism-webgis.jar' -WorkingDirectory '%CD%' -WindowStyle Hidden -RedirectStandardOutput 'runtime\application.log' -RedirectStandardError 'runtime\application-error.log' -PassThru;" ^
  "Set-Content -LiteralPath 'runtime\application-launcher.pid' -Value $p.Id"

ECHO Waiting for application...
FOR /L %%I IN (1,1,45) DO (
  POWERSHELL -NoProfile -Command "try { $r=Invoke-WebRequest -UseBasicParsing 'http://localhost:8080/api/health' -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } } catch {}; exit 1"
  IF NOT ERRORLEVEL 1 GOTO application_ready
  POWERSHELL -NoProfile -Command "Start-Sleep -Seconds 2"
)
ECHO Application failed to start. Check runtime\application-error.log.
TYPE runtime\application-error.log
PAUSE
EXIT /B 1

:application_ready
POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command ^
  "$processes = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*tourism-webgis.jar*' -and $_.Name -like 'java*' };" ^
  "if ($processes) { Set-Content -LiteralPath 'runtime\application.pid' -Value $processes.ProcessId }"
ECHO.
ECHO Xian Tourism WebGIS is ready: http://localhost:8080
IF /I NOT "%~1"=="--no-browser" START "" "http://localhost:8080"
EXIT /B 0
