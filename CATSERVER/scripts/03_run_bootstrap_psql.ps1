param(
    [string]$PsqlPath = "psql",
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$SuperUser = "postgres",
    [string]$BootstrapDatabase = "postgres",
    [string]$SqlRoot = "C:\CATGIS\CATSERVER\sql"
)

$psqlCommand = Get-Command $PsqlPath -ErrorAction SilentlyContinue
if (-not $psqlCommand) {
    throw "No se encontro psql. Instala PostgreSQL/psql o pasa -PsqlPath con la ruta completa."
}

$sqlFiles = @(
    "00_cluster_bootstrap.sql",
    "01_extensions_and_schemas.sql",
    "02_roles_and_privileges.sql",
    "03_admin_metadata.sql",
    "04_core_tables.sql",
    "05_seed_source_assets.sql"
)

foreach ($sqlFile in $sqlFiles) {
    $fullPath = Join-Path $SqlRoot $sqlFile
    if (-not (Test-Path -LiteralPath $fullPath)) {
        throw "Falta el archivo SQL requerido: $fullPath"
    }

    Write-Host "Ejecutando $sqlFile ..."
    & $psqlCommand.Source `
        -h $Host `
        -p $Port `
        -U $SuperUser `
        -d $BootstrapDatabase `
        -v ON_ERROR_STOP=1 `
        -f $fullPath

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo la ejecucion de $sqlFile"
    }
}

Write-Host ""
Write-Host "Bootstrap SQL completado para CATSERVER."

