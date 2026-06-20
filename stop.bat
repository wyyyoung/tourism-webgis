@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION
CD /D "%~dp0"

POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command ^
  "Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like '*tourism-webgis.jar*' -and $_.Name -like 'java*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"

IF EXIST "runtime\application.pid" DEL /Q "runtime\application.pid"
IF EXIST "runtime\application-launcher.pid" DEL /Q "runtime\application-launcher.pid"

docker compose stop postgis >NUL 2>NUL
ECHO Application and PostGIS have been stopped. Database data is preserved.
EXIT /B 0
