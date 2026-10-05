param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDxf,

    [Parameter(Mandatory = $true)]
    [string]$OutputDir,

    [string]$Ogr2OgrPath = "C:\Program Files\PostgreSQL\18\bin\ogr2ogr.exe",
    [string]$OgrInfoPath = "C:\Program Files\PostgreSQL\18\bin\ogrinfo.exe",
    [int]$SourceSrid = 22182,
    [int]$TargetSrid = 4326
)

function New-WhereClause {
    param(
        [string[]]$Layers,
        [string]$SubclassPredicate
    )

    $layerSql = ($Layers | ForEach-Object { "Layer = '" + $_.Replace("'", "''") + "'" }) -join " OR "
    return "(($layerSql) AND ($SubclassPredicate))"
}

function Export-Category {
    param(
        [string]$BaseName,
        [string]$WhereClause,
        [string]$GeometryType
    )

    $base22182 = Join-Path $OutputDir ($BaseName + "_22182.shp")
    $base4326 = Join-Path $OutputDir ($BaseName + "_4326.shp")

    foreach ($target in @($base22182, $base4326)) {
        $stem = [System.IO.Path]::GetFileNameWithoutExtension($target)
        Get-ChildItem -Path (Join-Path $OutputDir ($stem + ".*")) -ErrorAction SilentlyContinue | Remove-Item -Force
    }

    & $Ogr2OgrPath `
        -skipfailures `
        -f "ESRI Shapefile" `
        $base22182 `
        $SourceDxf `
        entities `
        -where $WhereClause `
        -a_srs ("EPSG:" + $SourceSrid) `
        -nlt $GeometryType

    if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $base22182)) {
        return $null
    }

    & $Ogr2OgrPath `
        -f "ESRI Shapefile" `
        $base4326 `
        $base22182 `
        -t_srs ("EPSG:" + $TargetSrid)

    if ($LASTEXITCODE -ne 0) {
        throw "No se pudo reproyectar $BaseName a EPSG:$TargetSrid"
    }

    $layerName = [System.IO.Path]::GetFileNameWithoutExtension($base4326)
    $info = & $OgrInfoPath -so $base4326 $layerName
    $infoText = ($info | Out-String).Trim()
    if ($infoText -match 'Feature Count:\s+0') {
        foreach ($target in @($base22182, $base4326)) {
            $stem = [System.IO.Path]::GetFileNameWithoutExtension($target)
            Get-ChildItem -Path (Join-Path $OutputDir ($stem + ".*")) -ErrorAction SilentlyContinue | Remove-Item -Force
        }
        return "EMPTY"
    }
    return $infoText
}

if (-not (Test-Path -LiteralPath $SourceDxf)) {
    throw "No existe el DXF fuente: $SourceDxf"
}

if (-not (Test-Path -LiteralPath $Ogr2OgrPath)) {
    throw "No se encontro ogr2ogr en: $Ogr2OgrPath"
}

if (-not (Test-Path -LiteralPath $OgrInfoPath)) {
    throw "No se encontro ogrinfo en: $OgrInfoPath"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$lineLayers = @(
    "Manzanas",
    "Loteo",
    "Limite de calles",
    "Limite de Barrios",
    "Sectores PCIA",
    "Sectores MCR",
    "Sectores sin Mensura",
    "Huellas - caminos",
    "Rutas y caminos principales",
    "Cauces de agua",
    "Alambrados",
    "Servidumbres",
    "Boulevares - canchas y otros",
    "Puerto y muelle",
    "Asentamiento irregular"
)

$textLayers = @(
    "Numero de Parcela",
    "Numero de Lote",
    "Nombre de Calles",
    "Nomenclatura PCIA",
    "Nomenclatura MCR",
    "Nombre de Barrios",
    "Referencias",
    "Rotulo",
    "Norte",
    "Kilometros"
)

$controlLayers = @(
    "Puntos con coordenadas",
    "0 - Puntos a relacionar",
    "Coordenadas",
    "Puntos Fijos"
)

$prefix = [System.IO.Path]::GetFileNameWithoutExtension($SourceDxf) -replace '[^A-Za-z0-9_-]+', '_'
$prefix = $prefix.Trim('_-')
if ([string]::IsNullOrWhiteSpace($prefix)) {
    $prefix = "cad"
}

$lineWhere = New-WhereClause -Layers $lineLayers -SubclassPredicate "SubClasses LIKE '%Polyline%' OR SubClasses LIKE '%Line%'"
$textWhere = New-WhereClause -Layers $textLayers -SubclassPredicate "SubClasses LIKE '%Text%'"
$controlWhere = New-WhereClause -Layers $controlLayers -SubclassPredicate "SubClasses LIKE '%Text%' OR SubClasses LIKE '%Point%'"

$results = @()
$results += [PSCustomObject]@{ Category = "lineas";   Info = Export-Category -BaseName ($prefix + "_lineas")  -WhereClause $lineWhere   -GeometryType "LINESTRING" }
$results += [PSCustomObject]@{ Category = "rotulos";  Info = Export-Category -BaseName ($prefix + "_rotulos") -WhereClause $textWhere   -GeometryType "POINT" }
$results += [PSCustomObject]@{ Category = "control";  Info = Export-Category -BaseName ($prefix + "_control") -WhereClause $controlWhere -GeometryType "POINT" }

$results | Format-Table -AutoSize
