@echo off
setlocal
cd /d "%~dp0"

if not exist reportes mkdir reportes
set LOG_FILE=reportes\instalador-exe.log

echo [CATGIS] Generando instalador EXE...
echo [CATGIS] Generando instalador EXE... > "%LOG_FILE%"

call gradle --no-daemon packageWindowsExeInstaller >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    echo [CATGIS] No se pudo generar el instalador EXE. Revisar "%LOG_FILE%".
    exit /b 1
)

echo [CATGIS] Instalador EXE generado correctamente. Ver "%LOG_FILE%".
exit /b 0
