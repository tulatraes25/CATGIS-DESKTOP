@echo off
setlocal
cd /d "%~dp0"

if not exist reportes mkdir reportes
set LOG_FILE=reportes\prueba-build.log

:: Add OSGeo4W GDAL to library path
set GDAL_BIN=C:\OSGeo4W64\bin
if exist "%GDAL_BIN%\gdalalljni.dll" (
    set JAVA_LIBRARY_PATH=%GDAL_BIN%
) else (
    set GDAL_BIN=C:\OSGeo4W\bin
    if exist "%GDAL_BIN%\gdalalljni.dll" set JAVA_LIBRARY_PATH=%GDAL_BIN%
)

echo [CATGIS] Iniciando validacion completa...
echo [CATGIS] Iniciando validacion completa... > "%LOG_FILE%"

call gradlew.bat --no-daemon clean test build >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    echo [CATGIS] La validacion fallo. Revisar "%LOG_FILE%".
    pause
    exit /b 1
)

echo [CATGIS] Validacion completa OK. Iniciando aplicacion...
echo [CATGIS] Validacion completa OK. Iniciando aplicacion... >> "%LOG_FILE%"
if defined JAVA_LIBRARY_PATH (
    start "CATGIS Desktop" gradlew.bat --no-daemon run -Djava.library.path=%JAVA_LIBRARY_PATH%
) else (
    start "CATGIS Desktop" gradlew.bat --no-daemon run
)
exit /b 0
