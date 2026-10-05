@echo off
setlocal
cd /d "%~dp0"
set "PORT=%~1"
if "%PORT%"=="" set "PORT=3080"
powershell -NoProfile -ExecutionPolicy RemoteSigned -File "%~dp0scripts\start.ps1" -Port %PORT%
endlocal
