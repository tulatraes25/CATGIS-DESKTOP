@echo off
cd /d "%~dp0catgis-desktop"
echo Iniciando CATGIS Desktop...
call .\gradlew.bat run --console=plain
pause
