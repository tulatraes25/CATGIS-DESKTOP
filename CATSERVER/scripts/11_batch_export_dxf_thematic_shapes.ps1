param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDir,

    [Parameter(Mandatory = $true)]
    [string]$OutputRoot,

    [string]$ExporterScript = "C:\CATGIS\CATSERVER\scripts\10_export_dxf_thematic_shapes.ps1",
    [string]$Filter = "*.dxf",
    [switch]$Recurse
)

if (-not (Test-Path -LiteralPath $SourceDir)) {
    throw "No existe la carpeta fuente: $SourceDir"
}

if (-not (Test-Path -LiteralPath $ExporterScript)) {
    throw "No existe el script exportador: $ExporterScript"
}

New-Item -ItemType Directory -Force -Path $OutputRoot | Out-Null

$files = Get-ChildItem -LiteralPath $SourceDir -Filter $Filter -File -Recurse:$Recurse | Sort-Object FullName
if (-not $files) {
    throw "No se encontraron DXF en $SourceDir"
}

$results = New-Object System.Collections.Generic.List[object]

foreach ($file in $files) {
    $folderName = ([System.IO.Path]::GetFileNameWithoutExtension($file.Name) -replace '[^A-Za-z0-9_-]+', '_').Trim('_-')
    if ([string]::IsNullOrWhiteSpace($folderName)) {
        $folderName = "cad"
    }
    $targetDir = Join-Path $OutputRoot $folderName

    try {
        $output = powershell -ExecutionPolicy Bypass -File $ExporterScript -SourceDxf $file.FullName -OutputDir $targetDir 2>&1
        $text = ($output | Out-String).Trim()
        $results.Add([PSCustomObject]@{
            SourceName = $file.Name
            SourcePath = $file.FullName
            Status     = "OK"
            OutputDir  = $targetDir
            Message    = $text
        })
    }
    catch {
        $results.Add([PSCustomObject]@{
            SourceName = $file.Name
            SourcePath = $file.FullName
            Status     = "ERROR"
            OutputDir  = $targetDir
            Message    = $_.Exception.Message
        })
    }
}

$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$csvPath = Join-Path $OutputRoot ("dxf_thematic_export_" + $stamp + ".csv")
$results | Export-Csv -LiteralPath $csvPath -NoTypeInformation -Encoding UTF8

$results | Select-Object SourceName,Status,OutputDir | Format-Table -AutoSize
Write-Host ""
Write-Host "Resumen CSV:"
Write-Host $csvPath
