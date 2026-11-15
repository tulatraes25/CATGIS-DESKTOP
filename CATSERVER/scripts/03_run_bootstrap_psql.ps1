param(
    [string]$PsqlPath = "psql",
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$SuperUser = "postgres",
    [string]$BootstrapDatabase = "postgres",
    [string]$Database = "CATSERVER",
    [string]$SqlRoot = ""
)

if ([string]::IsNullOrWhiteSpace($SqlRoot)) {
    $catserverRoot = Split-Path -Parent $PSScriptRoot
    $SqlRoot = Join-Path $catserverRoot "sql"
}

$psqlCommand = Get-Command $PsqlPath -ErrorAction SilentlyContinue
if (-not $psqlCommand) {
    throw "No se encontro psql. Instala PostgreSQL/psql o pasa -PsqlPath con la ruta completa."
}

$sqlFiles = @(
    "00_cluster_bootstrap.sql",
    "01_extensions_and_schemas.sql",
    "02_roles_and_privileges.sql",
    "03_admin_metadata.sql",
    "04_core_tables.sql"
)

foreach ($sqlFile in $sqlFiles) {
    $fullPath = Join-Path $SqlRoot $sqlFile
    if (-not (Test-Path -LiteralPath $fullPath)) {
        throw "Falta el archivo SQL requerido: $fullPath"
    }

    # 00 creates the database from the maintenance database; the remaining files
    # must target the created database because the SQL no longer forces \connect.
    $targetDatabase = if ($sqlFile -like "00_*") { $BootstrapDatabase } else { $Database }

    Write-Host "Ejecutando $sqlFile sobre $targetDatabase ..."
    & $psqlCommand.Source `
        -h $Host `
        -p $Port `
        -U $SuperUser `
        -d $targetDatabase `
        -v ON_ERROR_STOP=1 `
        -f $fullPath

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo la ejecucion de $sqlFile sobre $targetDatabase"
    }
}

Write-Host ""
Write-Host "Bootstrap SQL completado para CATSERVER (base: $Database)."

