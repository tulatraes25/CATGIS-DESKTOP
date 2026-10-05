param(
    [Parameter(Mandatory = $true)]
    [string]$SourceShape,

    [Parameter(Mandatory = $true)]
    [string]$MaskShape,

    [Parameter(Mandatory = $true)]
    [string]$OutputShape,

    [string]$Ogr2OgrPath = "C:\Program Files\PostgreSQL\18\bin\ogr2ogr.exe",
    [string]$OgrInfoPath = "C:\Program Files\PostgreSQL\18\bin\ogrinfo.exe"
)

if (-not (Test-Path -LiteralPath $SourceShape)) {
    throw "No existe el shape fuente: $SourceShape"
}

if (-not (Test-Path -LiteralPath $MaskShape)) {
    throw "No existe la mascara: $MaskShape"
}

$outDir = Split-Path -Parent $OutputShape
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$stem = [System.IO.Path]::GetFileNameWithoutExtension($OutputShape)
Get-ChildItem -Path (Join-Path $outDir ($stem + ".*")) -ErrorAction SilentlyContinue | Remove-Item -Force

$ogrOutput = & $Ogr2OgrPath `
    -f "ESRI Shapefile" `
    $OutputShape `
    $SourceShape `
    -clipsrc $MaskShape 2>&1

if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $OutputShape)) {
    $ogrText = ($ogrOutput | Out-String).Trim()
    if ($ogrText -match 'GEOS support not enabled') {
        throw "No se pudo recortar porque el ogr2ogr instalado no tiene soporte GEOS habilitado. El dato esta bien; la limitacion es del binario local."
    }
    throw "No se pudo recortar $SourceShape"
}

$layerName = [System.IO.Path]::GetFileNameWithoutExtension($OutputShape)
$info = & $OgrInfoPath -so $OutputShape $layerName
$infoText = ($info | Out-String).Trim()
if ($infoText -match 'Feature Count:\s+0') {
    Get-ChildItem -Path (Join-Path $outDir ($stem + ".*")) -ErrorAction SilentlyContinue | Remove-Item -Force
    Write-Host "EMPTY"
}
else {
    Write-Host $infoText
}
