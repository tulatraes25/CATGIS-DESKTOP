param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDir,

    [Parameter(Mandatory = $true)]
    [string]$OutputDir,

    [string]$ConverterScript = "C:\CATGIS\CATSERVER\scripts\08_convert_dwg_safe.ps1",
    [string]$Filter = "*.dwg",
    [switch]$Recurse
)

if (-not (Test-Path -LiteralPath $SourceDir)) {
    throw "No existe la carpeta fuente: $SourceDir"
}

if (-not (Test-Path -LiteralPath $ConverterScript)) {
    throw "No existe el script convertidor: $ConverterScript"
}

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$files = Get-ChildItem -LiteralPath $SourceDir -Filter $Filter -File -Recurse:$Recurse | Sort-Object FullName
if (-not $files) {
    throw "No se encontraron DWG en $SourceDir"
}

$results = New-Object System.Collections.Generic.List[object]

foreach ($file in $files) {
    try {
        $output = powershell -ExecutionPolicy Bypass -File $ConverterScript -SourceDwg $file.FullName -OutputDir $OutputDir 2>&1
        $generatedPath = $output | Select-Object -Last 1
        $generated = if ($generatedPath -and (Test-Path -LiteralPath $generatedPath)) { Get-Item -LiteralPath $generatedPath } else { $null }

        $results.Add([PSCustomObject]@{
            SourceName    = $file.Name
            SourcePath    = $file.FullName
            Status        = if ($generated) { "OK" } else { "UNKNOWN" }
            OutputName    = if ($generated) { $generated.Name } else { "" }
            OutputPath    = if ($generated) { $generated.FullName } else { "" }
            OutputBytes   = if ($generated) { $generated.Length } else { $null }
            Message       = ($output | Out-String).Trim()
        })
    }
    catch {
        $results.Add([PSCustomObject]@{
            SourceName    = $file.Name
            SourcePath    = $file.FullName
            Status        = "ERROR"
            OutputName    = ""
            OutputPath    = ""
            OutputBytes   = $null
            Message       = $_.Exception.Message
        })
    }
}

$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$csvPath = Join-Path $OutputDir ("dwg_batch_conversion_" + $stamp + ".csv")
$results | Export-Csv -LiteralPath $csvPath -NoTypeInformation -Encoding UTF8

$results | Select-Object SourceName,Status,OutputName,OutputBytes | Format-Table -AutoSize
Write-Host ""
Write-Host "Resumen CSV:"
Write-Host $csvPath
