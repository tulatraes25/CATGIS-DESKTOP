param(
    [Parameter(Mandatory = $true)]
    [string]$SourceRoot,

    [Parameter(Mandatory = $true)]
    [string]$MaskShape,

    [string]$Filter = "*_4326.shp",
    [string]$ClipperScript = "C:\CATGIS\CATSERVER\scripts\12_clip_shapefile_to_mask.ps1"
)

if (-not (Test-Path -LiteralPath $SourceRoot)) {
    throw "No existe la carpeta fuente: $SourceRoot"
}

if (-not (Test-Path -LiteralPath $MaskShape)) {
    throw "No existe la mascara: $MaskShape"
}

if (-not (Test-Path -LiteralPath $ClipperScript)) {
    throw "No existe el script clipper: $ClipperScript"
}

$files = Get-ChildItem -Path $SourceRoot -Recurse -Filter $Filter -File | Sort-Object FullName
if (-not $files) {
    throw "No se encontraron shapes para recortar en $SourceRoot"
}

$results = New-Object System.Collections.Generic.List[object]

foreach ($file in $files) {
    $baseName = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $target = Join-Path $file.DirectoryName ($baseName -replace '_4326$', '_ejido_4326').ToString() + '.shp'
    try {
        $output = powershell -ExecutionPolicy Bypass -File $ClipperScript -SourceShape $file.FullName -MaskShape $MaskShape -OutputShape $target 2>&1
        $results.Add([PSCustomObject]@{
            SourceShape = $file.FullName
            OutputShape = $target
            Status      = if (($output | Out-String) -match 'EMPTY') { 'EMPTY' } else { 'OK' }
            Message     = ($output | Out-String).Trim()
        })
    }
    catch {
        $results.Add([PSCustomObject]@{
            SourceShape = $file.FullName
            OutputShape = $target
            Status      = 'ERROR'
            Message     = $_.Exception.Message
        })
    }
}

$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$csvPath = Join-Path $SourceRoot ("shape_clip_ejido_" + $stamp + ".csv")
$results | Export-Csv -LiteralPath $csvPath -NoTypeInformation -Encoding UTF8

$results | Select-Object Status,SourceShape,OutputShape | Format-Table -AutoSize
Write-Host ""
Write-Host "Resumen CSV:"
Write-Host $csvPath
