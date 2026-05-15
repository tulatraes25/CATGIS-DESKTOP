param(
    [string]$SourcePath = "C:\Users\tes\OneDrive\Documentos\pozos_cr.kml",
    [string]$LayerName = "pozos_fciv",
    [string]$Database = "catserver",
    [string]$DbHost = "localhost",
    [int]$Port = 5432,
    [string]$User = "postgres",
    [string]$Password = "",
    [string]$RawSchema = "raw",
    [string]$RawTable = "pozos_petroleros_cr_kml",
    [string]$SqlPath = "C:\CATGIS\CATSERVER\sql\21_pozos_petroleros_ejido.sql",
    [string]$Ogr2OgrPath = "C:\OSGeo4W\bin\ogr2ogr.exe",
    [string]$PsqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe",
    [string]$Pgsql2ShpPath = "C:\Program Files\PostgreSQL\18\bin\pgsql2shp.exe",
    [string]$ShapeOutputPath = "C:\CATGIS\catserver_shapes\export\pozos_petroleros\pozos_petroleros_ejido.shp"
)

if (-not (Test-Path -LiteralPath $SourcePath)) {
    throw "No existe el KML fuente: $SourcePath"
}

foreach ($toolPath in @($Ogr2OgrPath, $PsqlPath, $Pgsql2ShpPath, $SqlPath)) {
    if (-not (Test-Path -LiteralPath $toolPath)) {
        throw "No existe el recurso requerido: $toolPath"
    }
}

$connParts = @(
    "host=$DbHost",
    "port=$Port",
    "dbname=$Database",
    "user=$User"
)

if (-not [string]::IsNullOrWhiteSpace($Password)) {
    $connParts += "password=$Password"
}

$pgConnection = "PG:" + ($connParts -join " ")
$layerTarget = "$RawSchema.$RawTable"
$previousPassword = $env:PGPASSWORD

if (-not [string]::IsNullOrWhiteSpace($Password)) {
    $env:PGPASSWORD = $Password
}

$previousGdalData = $env:GDAL_DATA
$previousProjLib = $env:PROJ_LIB
$previousPath = $env:PATH

try {
    $env:GDAL_DATA = "C:\OSGeo4W\share\gdal"
    $env:PROJ_LIB = "C:\OSGeo4W\share\proj"
    $env:PATH = "C:\OSGeo4W\bin;C:\OSGeo4W\apps\gdal-dev\bin;C:\Program Files\PostgreSQL\18\bin;" + $previousPath

    Write-Host "Importando $LayerName hacia $layerTarget"
    & $Ogr2OgrPath `
        -overwrite `
        -f PostgreSQL `
        $pgConnection `
        $SourcePath `
        $LayerName `
        -nln $layerTarget `
        -nlt POINT `
        -dim XY `
        -lco GEOMETRY_NAME=geom `
        -lco FID=ogc_fid

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo ogr2ogr al importar $SourcePath"
    }

    Write-Host "Construyendo tabla final dentro del ejido"
    & $PsqlPath -h $DbHost -p $Port -U $User -d $Database -v ON_ERROR_STOP=1 -f $SqlPath

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo psql al ejecutar $SqlPath"
    }

    $targetDir = Split-Path -Parent $ShapeOutputPath
    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($ShapeOutputPath)
    Get-ChildItem -Path (Join-Path $targetDir ($baseName + ".*")) -ErrorAction SilentlyContinue | Remove-Item -Force

    Write-Host "Exportando shape operativo"
    & $Pgsql2ShpPath `
        -f $ShapeOutputPath `
        -h $DbHost `
        -p $Port `
        -u $User `
        -P $Password `
        -g geom `
        -r `
        $Database `
        "control_urbano_operativo.pozos_petroleros_ejido"

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo pgsql2shp al exportar $ShapeOutputPath"
    }
}
finally {
    $env:GDAL_DATA = $previousGdalData
    $env:PROJ_LIB = $previousProjLib
    $env:PATH = $previousPath

    if (-not [string]::IsNullOrWhiteSpace($Password)) {
        if ($null -ne $previousPassword) {
            $env:PGPASSWORD = $previousPassword
        }
        else {
            Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
        }
    }
}

Write-Host "Proceso completado para pozos petroleros dentro del ejido"
