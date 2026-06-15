@echo off
setlocal
cd /d "%~dp0"

if not exist reportes mkdir reportes
set LOG_FILE=reportes\prueba-build.log

echo [CATGIS] Iniciando validacion completa...
echo [CATGIS] Iniciando validacion completa... > "%LOG_FILE%"

call gradlew.bat --no-daemon clean test build >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    echo [CATGIS] La validacion fallo. Revisar "%LOG_FILE%".
    exit /b 1
)

echo [CATGIS] Validacion completa OK. Iniciando aplicacion...
echo [CATGIS] Validacion completa OK. Iniciando aplicacion... >> "%LOG_FILE%"
start "CATGIS Desktop" gradlew.bat --no-daemon run
exit /b 0
