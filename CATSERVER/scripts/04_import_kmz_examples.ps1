param(
    [string]$Ogr2OgrPath = "ogr2ogr",
    [string]$Host = "localhost",
    [int]$Port = 5432,
    [string]$Database = "CATSERVER",
    [string]$User = "catserver_etl",
    [string]$Password = "CAMBIAR",
    [string]$Schema = "raw",
    [string]$BatchDate = "20260413"
)

$ogr = Get-Command $Ogr2OgrPath -ErrorAction SilentlyContinue
if (-not $ogr) {
    throw "No se encontro ogr2ogr. Instala GDAL/QGIS y volve a ejecutar."
}

$items = @(
    @{ Path = "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Parcelas.kmz"; Target = "kmz_parcelas_$BatchDate" },
    @{ Path = "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Limite de Barrios.kmz"; Target = "kmz_limite_barrios_$BatchDate" },
    @{ Path = "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Circunscripciones - Sectores.kmz"; Target = "kmz_circunscripciones_sectores_$BatchDate" },
    @{ Path = "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Zonificacion por Ordenanza.kmz"; Target = "kmz_zonificacion_ordenanza_$BatchDate" },
    @{ Path = "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Drenaje Comodoro Rivadavia.kmz"; Target = "kmz_drenaje_$BatchDate" },
    @{ Path = "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Lineas electricas.kmz"; Target = "kmz_lineas_electricas_$BatchDate" },
    @{ Path = "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP\KMZ\Reservorios.kmz"; Target = "kmz_reservorios_$BatchDate" }
)

foreach ($item in $items) {
    if (-not (Test-Path -LiteralPath $item.Path)) {
        Write-Warning "No existe: $($item.Path)"
        continue
    }

    $pgConnection = "PG:host=$Host port=$Port dbname=$Database user=$User password=$Password"
    $targetLayer = "$Schema.$($item.Target)"

    Write-Host "Importando $($item.Path) -> $targetLayer"

    & $ogr.Source `
        -f "PostgreSQL" `
        $pgConnection `
        $item.Path `
        -nln $targetLayer `
        -lco GEOMETRY_NAME=geom `
        -lco FID=gid `
        -nlt PROMOTE_TO_MULTI `
        -overwrite

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo la importacion de $($item.Path)"
    }
}

Write-Host "Importacion KMZ finalizada."

