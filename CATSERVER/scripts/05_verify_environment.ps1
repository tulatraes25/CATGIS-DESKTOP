$catgisCandidates = @(
    "C:\CATGIS\catgis-desktop\build\install\catgis-desktop\bin\catgis-desktop.bat",
    "C:\CATGIS\catgis-desktop\build\installer\windows\app-image\CATGIS Desktop\CATGIS Desktop.exe",
    "C:\CATGIS\catgis-desktop\build\installer\windows\app-image-rev6-20260410b\CATGIS Desktop\CATGIS Desktop.exe"
)

$cadConverterCandidates = @(
    "C:\CATGIS\tools\oda\app\ODAFileConverter.exe",
    "C:\CATGIS\tools\oda\ODAFileConverter.exe",
    "C:\Program Files\ODA\ODAFileConverter\ODAFileConverter.exe",
    "C:\Program Files\ODA\ODAFileConverter 25.12.0\ODAFileConverter.exe",
    "C:\Program Files\ODA\ODAFileConverter 27.1\ODAFileConverter.exe",
    "C:\Program Files\Teigha File Converter\TeighaFileConverter.exe",
    "C:\Program Files\Autodesk\DWG TrueView\DWGCONVERT.exe"
)

function Find-CandidatePath {
    param(
        [string[]]$Candidates
    )

    foreach ($candidate in $Candidates) {
        if (Test-Path -LiteralPath $candidate) {
            return $candidate
        }
    }
    return ""
}

$tools = @(
    @{ Name = "psql"; ExpectedHint = "PostgreSQL client"; Required = $true },
    @{ Name = "pg_dump"; ExpectedHint = "PostgreSQL backup"; Required = $true },
    @{ Name = "pg_restore"; ExpectedHint = "PostgreSQL restore"; Required = $true },
    @{ Name = "ogr2ogr"; ExpectedHint = "GDAL/OGR import-export"; Required = $false },
    @{ Name = "gdalinfo"; ExpectedHint = "GDAL info"; Required = $false }
)

$results = foreach ($tool in $tools) {
    $cmd = Get-Command $tool.Name -ErrorAction SilentlyContinue
    [PSCustomObject]@{
        Tool         = $tool.Name
        Status       = if ($cmd) { "OK" } elseif ($tool.Required) { "MISSING" } else { "OPTIONAL" }
        Path         = if ($cmd) { $cmd.Source } else { "" }
        ExpectedHint = $tool.ExpectedHint
        Required     = $tool.Required
    }
}

$catgisPath = Find-CandidatePath -Candidates $catgisCandidates
$cadConverterPath = Find-CandidatePath -Candidates $cadConverterCandidates

$extraResults = @(
    [PSCustomObject]@{
        Tool         = "catgis_desktop"
        Status       = if ($catgisPath) { "OK" } else { "MISSING" }
        Path         = $catgisPath
        ExpectedHint = "CATGIS desktop"
    },
    [PSCustomObject]@{
        Tool         = "cad_converter"
        Status       = if ($cadConverterPath) { "OK" } else { "OPTIONAL" }
        Path         = $cadConverterPath
        ExpectedHint = "ODA / Teigha / DWG converter"
    }
)

$results | Format-Table -AutoSize
$extraResults | Format-Table -AutoSize

$missing = @()
$missing += $results | Where-Object { $_.Status -eq "MISSING" -and $_.Required }
$missing += $extraResults | Where-Object { $_.Tool -eq "catgis_desktop" -and $_.Status -eq "MISSING" }

if ($missing.Count -gt 0) {
    Write-Host ""
    Write-Host "Faltan herramientas:" -ForegroundColor Yellow
    $missing | Select-Object Tool, ExpectedHint | Format-Table -AutoSize
    exit 1
}

Write-Host ""
Write-Host "Entorno listo para bootstrap de CATSERVER." -ForegroundColor Green
