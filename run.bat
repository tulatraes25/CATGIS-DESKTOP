@echo off
setlocal

cd /d "%~dp0catgis-desktop" || (
    echo [CATGIS] No se pudo entrar a "%~dp0catgis-desktop".
    pause
    exit /b 1
)

if not exist reportes mkdir reportes
set "LOG_FILE=reportes\inicio-catgis.log"
set "APP_IMAGE_DIR=build\installer\windows\app-image\CATGIS Desktop"
set "APP_IMAGE_MAIN_JAR=build\installer\windows\app-image\CATGIS Desktop\app\catgis-desktop-1.0.jar"
set "LAUNCHER_PS1=scripts\Launch-Catgis.ps1"
set "JAVA_LIBRARY_PATH="
set "GDAL_BIN="
set "JAVA_EXE="

if exist "C:\OSGeo4W64\bin\gdalalljni.dll" (
    set "GDAL_BIN=C:\OSGeo4W64\bin"
) else if exist "C:\OSGeo4W\bin\gdalalljni.dll" (
    set "GDAL_BIN=C:\OSGeo4W\bin"
)

echo [CATGIS] Iniciando CATGIS Desktop...
echo [CATGIS] Iniciando CATGIS Desktop... > "%LOG_FILE%"
if defined GDAL_BIN (
    set "JAVA_LIBRARY_PATH=%GDAL_BIN%"
    echo [CATGIS] GDAL detectado en "%GDAL_BIN%".
    echo [CATGIS] GDAL_BIN=%GDAL_BIN%>> "%LOG_FILE%"
) else (
    echo [CATGIS] GDAL nativo no encontrado. Se iniciara sin plugins GDAL.
    echo [CATGIS] GDAL nativo no encontrado. Se iniciara sin plugins GDAL.>> "%LOG_FILE%"
)

if not exist "%APP_IMAGE_MAIN_JAR%" (
    echo [CATGIS] Runtime local no encontrado. Generando app-image...
    echo [CATGIS] Runtime local no encontrado. Generando app-image...>> "%LOG_FILE%"
    call gradlew.bat --no-daemon packageWindowsAppImage >> "%LOG_FILE%" 2>&1
    if errorlevel 1 (
        echo [CATGIS] No se pudo generar la app-image. Revisar "%CD%\%LOG_FILE%".
        pause
        exit /b 1
    )
)

if not exist "%APP_IMAGE_MAIN_JAR%" (
    echo [CATGIS] No se encontro el runtime Java de la app en "%CD%\%APP_IMAGE_DIR%\app".
    echo [CATGIS] No se encontro el runtime Java de la app en "%CD%\%APP_IMAGE_DIR%\app".>> "%LOG_FILE%"
    pause
    exit /b 1
)

if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javaw.exe" (
    set "JAVA_EXE=%JAVA_HOME%\bin\javaw.exe"
)

if not defined JAVA_EXE if exist "C:\Program Files\Java\jdk-21.0.10\bin\javaw.exe" (
    set "JAVA_EXE=C:\Program Files\Java\jdk-21.0.10\bin\javaw.exe"
)

if not defined JAVA_EXE (
    for /f "delims=" %%I in ('powershell -NoProfile -ExecutionPolicy Bypass -Command "$c = Get-ChildItem 'C:\Program Files\Java' -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue ^| Sort-Object Name -Descending ^| Select-Object -First 1 -ExpandProperty FullName; if ($c) { Join-Path $c 'bin\javaw.exe' }"') do (
        if exist "%%I" set "JAVA_EXE=%%I"
    )
)

if not defined JAVA_EXE (
    echo [CATGIS] No se encontro javaw.exe. Instala un JDK o define JAVA_HOME.
    echo [CATGIS] No se encontro javaw.exe. Instala un JDK o define JAVA_HOME.>> "%LOG_FILE%"
    pause
    exit /b 1
)

echo [CATGIS] Java detectado en "%JAVA_EXE%".
echo [CATGIS] JAVA_EXE=%JAVA_EXE%>> "%LOG_FILE%"

if not exist "%LAUNCHER_PS1%" (
    echo [CATGIS] Falta "%CD%\%LAUNCHER_PS1%".
    echo [CATGIS] Falta "%CD%\%LAUNCHER_PS1%".>> "%LOG_FILE%"
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%LAUNCHER_PS1%" -JavaExe "%JAVA_EXE%" -WorkingDir "%CD%\%APP_IMAGE_DIR%" -JavaLibraryPath "%JAVA_LIBRARY_PATH%" >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    echo [CATGIS] Fallo el lanzamiento Java. Revisar "%CD%\%LOG_FILE%".
    pause
    exit /b 1
)
echo [CATGIS] Lanzamiento enviado via javaw.>> "%LOG_FILE%"

echo [CATGIS] Lanzamiento enviado. Si no aparece la ventana, revisar "%CD%\%LOG_FILE%".
exit /b 0
