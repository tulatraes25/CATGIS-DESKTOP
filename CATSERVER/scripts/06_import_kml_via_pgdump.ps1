param(
    [Parameter(Mandatory = $true)]
    [string]$SourcePath,

    [Parameter(Mandatory = $true)]
    [string]$LayerName,

    [Parameter(Mandatory = $true)]
    [string]$Schema,

    [Parameter(Mandatory = $true)]
    [string]$Table,

    [string]$Database = "catserver",
    [string]$User = "postgres",
    [string]$Password = "",
    [string]$TempDir = "C:\CATGIS\catserver_tmp\pgdump",
    [string]$Ogr2OgrPath = "C:\Program Files\PostgreSQL\18\bin\ogr2ogr.exe",
    [string]$PsqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
)

if (-not (Test-Path -LiteralPath $SourcePath)) {
    throw "No existe el archivo fuente: $SourcePath"
}

if (-not (Test-Path -LiteralPath $Ogr2OgrPath)) {
    throw "No se encontro ogr2ogr en: $Ogr2OgrPath"
}

if (-not (Test-Path -LiteralPath $PsqlPath)) {
    throw "No se encontro psql en: $PsqlPath"
}

New-Item -ItemType Directory -Force -Path $TempDir | Out-Null

$sqlPath = Join-Path $TempDir ($Table + ".sql")
$cleanPath = Join-Path $TempDir ($Table + "_clean.sql")
$layerTarget = "$Schema.$Table"

Write-Host "Generando PGDump: $SourcePath -> $layerTarget"

& $Ogr2OgrPath `
    -f PGDump `
    $sqlPath `
    $SourcePath `
    $LayerName `
    -nln $layerTarget `
    -nlt PROMOTE_TO_MULTI

if ($LASTEXITCODE -ne 0) {
    throw "Fallo ogr2ogr al generar el PGDump de $SourcePath"
}

$sqlLines = Get-Content -LiteralPath $sqlPath -Encoding UTF8 |
    Where-Object { $_ -notmatch '^CREATE SCHEMA ' }
if ($null -eq $sqlLines) {
    $sqlLines = @()
}
# El dump de ogr2ogr esta en UTF8; escribirlo como ascii corrompia los
# atributos con acentos. Se escribe UTF8 sin BOM, que psql interpreta bien.
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllLines($cleanPath, [string[]]$sqlLines, $utf8NoBom)

$previousPassword = $env:PGPASSWORD
if ($Password) {
    $env:PGPASSWORD = $Password
}

try {
    Write-Host "Cargando en PostGIS: $layerTarget"
    & $PsqlPath -U $User -d $Database -v ON_ERROR_STOP=1 -f $cleanPath

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo psql al cargar $layerTarget"
    }
}
finally {
    if ($Password) {
        if ($null -ne $previousPassword) {
            $env:PGPASSWORD = $previousPassword
        }
        else {
            Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
        }
    }
}

Write-Host "Importacion completada para $layerTarget"
