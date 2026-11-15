@echo off
setlocal
cd /d "%~dp0"

if not exist reportes mkdir reportes
set LOG_FILE=reportes\prueba-build.log

:: Add OSGeo4W GDAL to library path (respeta CATGIS_OSGEO4W)
set JAVA_LIBRARY_PATH=
if defined CATGIS_OSGEO4W if exist "%CATGIS_OSGEO4W%\bin\gdalalljni.dll" set JAVA_LIBRARY_PATH=%CATGIS_OSGEO4W%\bin
if not defined JAVA_LIBRARY_PATH if exist "C:\OSGeo4W64\bin\gdalalljni.dll" set JAVA_LIBRARY_PATH=C:\OSGeo4W64\bin
if not defined JAVA_LIBRARY_PATH if exist "C:\OSGeo4W\bin\gdalalljni.dll" set JAVA_LIBRARY_PATH=C:\OSGeo4W\bin

echo [CATGIS] Iniciando validacion completa...
echo [CATGIS] Iniciando validacion completa... > "%LOG_FILE%"

call gradlew.bat --no-daemon compileJava test --rerun-tasks >> "%LOG_FILE%" 2>&1
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
