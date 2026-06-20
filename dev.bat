@ECHO OFF
SETLOCAL
CD /D "%~dp0"

docker compose up -d postgis
IF ERRORLEVEL 1 EXIT /B 1

START "WebGIS Backend" CMD /K "CD /D %CD%\tourism-webgis-backend && mvnw.cmd spring-boot:run"
START "WebGIS Frontend" CMD /K "CD /D %CD%\tourism-webgis-frontend && npm run dev"
EXIT /B 0

