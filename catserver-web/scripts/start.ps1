param(
    [int]$Port = 3082
)

$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$env:CATSERVER_WEB_PORT = [string]$Port

Write-Host "CATSERVER Web iniciando en http://localhost:$Port"
npm start
