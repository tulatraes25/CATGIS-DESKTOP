@echo off
cd /d C:\CATGIS\catserver-web
powershell -NoProfile -ExecutionPolicy RemoteSigned -File "C:\CATGIS\catserver-web\scripts\start.ps1" -Port 3082
