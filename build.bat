@ECHO OFF
SETLOCAL
CD /D "%~dp0"

ECHO [1/4] Checking Node.js...
WHERE node >NUL 2>NUL
IF ERRORLEVEL 1 (
  ECHO Node.js 22 or newer is required to build the frontend.
  EXIT /B 1
)

ECHO [2/4] Building Vue and Cesium frontend...
PUSHD tourism-webgis-frontend
CALL npm install
IF ERRORLEVEL 1 EXIT /B 1
CALL npm run build
IF ERRORLEVEL 1 EXIT /B 1
POPD

ECHO [3/4] Copying frontend into Spring Boot...
POWERSHELL -NoProfile -ExecutionPolicy Bypass -Command ^
  "$target = Join-Path '%CD%' 'tourism-webgis-backend\src\main\resources\static';" ^
  "if (Test-Path $target) { Remove-Item -LiteralPath $target -Recurse -Force };" ^
  "New-Item -ItemType Directory -Path $target -Force | Out-Null;" ^
  "Copy-Item -Path (Join-Path '%CD%' 'tourism-webgis-frontend\dist\*') -Destination $target -Recurse -Force"
IF ERRORLEVEL 1 EXIT /B 1

ECHO [4/4] Packaging Spring Boot...
PUSHD tourism-webgis-backend
CALL mvnw.cmd clean package
IF ERRORLEVEL 1 EXIT /B 1
POPD

IF NOT EXIST runtime MKDIR runtime
COPY /Y "tourism-webgis-backend\target\tourism-webgis.jar" "runtime\tourism-webgis.jar" >NUL

ECHO.
ECHO Build completed: runtime\tourism-webgis.jar
EXIT /B 0

