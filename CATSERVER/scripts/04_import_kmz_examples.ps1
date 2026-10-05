param(
    [string]$Ogr2OgrPath = "ogr2ogr",
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$Database = "CATSERVER",
    [string]$User = "catserver_etl",
    [string]$Password = "",
    [string]$Schema = "raw",
    [string]$BatchDate = "20260413",
    [Parameter(Mandatory = $true)]
    [string]$SourceRoot
)

$ogr = Get-Command $Ogr2OgrPath -ErrorAction SilentlyContinue
if (-not $ogr) {
    throw "No se encontro ogr2ogr. Instala GDAL/QGIS y volve a ejecutar."
}

$items = @(
    @{ Name = "Parcelas.kmz"; Target = "kmz_parcelas_$BatchDate" },
    @{ Name = "Limite de Barrios.kmz"; Target = "kmz_limite_barrios_$BatchDate" },
    @{ Name = "Circunscripciones - Sectores.kmz"; Target = "kmz_circunscripciones_sectores_$BatchDate" },
    @{ Name = "Zonificacion por Ordenanza.kmz"; Target = "kmz_zonificacion_ordenanza_$BatchDate" },
    @{ Name = "Drenaje Comodoro Rivadavia.kmz"; Target = "kmz_drenaje_$BatchDate" },
    @{ Name = "Lineas electricas.kmz"; Target = "kmz_lineas_electricas_$BatchDate" },
    @{ Name = "Reservorios.kmz"; Target = "kmz_reservorios_$BatchDate" }
)

if (-not (Test-Path -LiteralPath $SourceRoot)) {
    throw "No existe el directorio fuente: $SourceRoot"
}

$previousPassword = $env:PGPASSWORD
if (-not [string]::IsNullOrWhiteSpace($Password)) {
    $env:PGPASSWORD = $Password
}

try {
foreach ($item in $items) {
    $sourcePath = Join-Path $SourceRoot $item.Name
    if (-not (Test-Path -LiteralPath $sourcePath)) {
        Write-Warning "No existe: $($sourcePath)"
        continue
    }

    $pgConnection = "PG:host=$Host port=$Port dbname=$Database user=$User"
    $targetLayer = "$Schema.$($item.Target)"

    Write-Host "Importando $($sourcePath) -> $targetLayer"

    & $ogr.Source `
        -f "PostgreSQL" `
        $pgConnection `
        $sourcePath `
        -nln $targetLayer `
        -lco GEOMETRY_NAME=geom `
        -lco FID=gid `
        -nlt PROMOTE_TO_MULTI `
        -overwrite

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo la importacion de $($sourcePath)"
    }
}
}
finally {
    if (-not [string]::IsNullOrWhiteSpace($Password)) {
        if ($null -ne $previousPassword) {
            $env:PGPASSWORD = $previousPassword
        }
        else {
            Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
        }
    }
}

Write-Host "Importacion KMZ finalizada."

