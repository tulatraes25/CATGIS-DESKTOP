param(
    [string]$RootPath = "C:\CATSERVER"
)

$directories = @(
    $RootPath,
    (Join-Path $RootPath "incoming"),
    (Join-Path $RootPath "incoming\dwg"),
    (Join-Path $RootPath "incoming\kmz"),
    (Join-Path $RootPath "incoming\cad_backup"),
    (Join-Path $RootPath "incoming\other"),
    (Join-Path $RootPath "inventory"),
    (Join-Path $RootPath "etl"),
    (Join-Path $RootPath "etl\scratch"),
    (Join-Path $RootPath "etl\rejected"),
    (Join-Path $RootPath "exports"),
    (Join-Path $RootPath "exports\gpkg"),
    (Join-Path $RootPath "exports\csv"),
    (Join-Path $RootPath "backups"),
    (Join-Path $RootPath "backups\logical"),
    (Join-Path $RootPath "backups\physical"),
    (Join-Path $RootPath "logs"),
    (Join-Path $RootPath "docs"),
    (Join-Path $RootPath "sql")
)

foreach ($directory in $directories) {
    if (-not (Test-Path -LiteralPath $directory)) {
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
        Write-Host "Creado: $directory"
    } else {
        Write-Host "Ya existe: $directory"
    }
}

Write-Host ""
Write-Host "Estructura base lista para CATSERVER en: $RootPath"

