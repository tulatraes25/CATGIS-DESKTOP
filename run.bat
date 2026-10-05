@echo off
setlocal
cd /d "%~dp0catgis-desktop"

echo [CATGIS] Iniciando CATGIS Desktop...
if exist "C:\OSGeo4W64\bin\gdalalljni.dll" set GDAL_BIN=C:\OSGeo4W64\bin
if exist "C:\OSGeo4W\bin\gdalalljni.dll" set GDAL_BIN=C:\OSGeo4W\bin

if defined GDAL_BIN (
    echo [CATGIS] GDAL detectado en "%GDAL_BIN%".
    start "CATGIS Desktop" gradlew.bat --no-daemon run --rerun-tasks -Djava.library.path=%GDAL_BIN%
) else (
    echo [CATGIS] GDAL no encontrado. Iniciando sin GDAL.
    start "CATGIS Desktop" gradlew.bat --no-daemon run --rerun-tasks
)
exit /b 0
