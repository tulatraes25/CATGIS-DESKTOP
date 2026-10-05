@echo off
setlocal
cd /d "%~dp0"

if not exist reportes mkdir reportes
set LOG_FILE=reportes\app-image.log

echo [CATGIS] Generando app-image Windows...
echo [CATGIS] Generando app-image Windows... > "%LOG_FILE%"

call gradlew.bat --no-daemon packageWindowsAppImage >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    echo [CATGIS] No se pudo generar la app-image. Revisar "%LOG_FILE%".
    exit /b 1
)

echo [CATGIS] App-image generada correctamente. Ver "%LOG_FILE%".
exit /b 0
