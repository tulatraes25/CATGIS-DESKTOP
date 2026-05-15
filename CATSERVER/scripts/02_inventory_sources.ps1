param(
    [string]$OutputCsv = "C:\CATGIS\CATSERVER\inventory\source_inventory_20260413.csv"
)

function Get-SignatureText {
    param(
        [string]$Path,
        [int]$MaxBytes = 6
    )

    try {
        $bytes = Get-Content -LiteralPath $Path -Encoding Byte -TotalCount $MaxBytes -ErrorAction Stop
        if (-not $bytes) {
            return ""
        }

        $chars = foreach ($byte in $bytes) {
            if ($byte -ge 32 -and $byte -le 126) {
                [char]$byte
            } else {
                "."
            }
        }
        return (-join $chars)
    } catch {
        return ""
    }
}

function Get-SourceRows {
    param(
        [string]$GroupName,
        [string]$LiteralPath
    )

    if (-not (Test-Path -LiteralPath $LiteralPath)) {
        return [PSCustomObject]@{
            SourceGroup   = $GroupName
            FullName      = $LiteralPath
            Exists        = $false
            Extension     = ""
            SizeBytes     = $null
            LastWriteTime = $null
            Signature     = ""
            Category      = "missing"
        }
    }

    $item = Get-Item -LiteralPath $LiteralPath -ErrorAction Stop
    if ($item.PSIsContainer) {
        Get-ChildItem -LiteralPath $LiteralPath -Recurse -File -ErrorAction SilentlyContinue | ForEach-Object {
            $signature = Get-SignatureText -Path $_.FullName
            $category = switch ($_.Extension.ToLowerInvariant()) {
                ".dwg" { "dwg" }
                ".bak" { if ($signature -like "AC*") { "cad_backup" } else { "bak" } }
                ".kmz" { "kmz" }
                ".kml" { "kml" }
                default { "other" }
            }

            [PSCustomObject]@{
                SourceGroup   = $GroupName
                FullName      = $_.FullName
                Exists        = $true
                Extension     = $_.Extension
                SizeBytes     = $_.Length
                LastWriteTime = $_.LastWriteTime
                Signature     = $signature
                Category      = $category
            }
        }
        return
    }

    $signature = Get-SignatureText -Path $item.FullName
    $category = switch ($item.Extension.ToLowerInvariant()) {
        ".dwg" { "dwg" }
        ".bak" { if ($signature -like "AC*") { "cad_backup" } else { "bak" } }
        ".kmz" { "kmz" }
        ".kml" { "kml" }
        default { "other" }
    }

    [PSCustomObject]@{
        SourceGroup   = $GroupName
        FullName      = $item.FullName
        Exists        = $true
        Extension     = $item.Extension
        SizeBytes     = $item.Length
        LastWriteTime = $item.LastWriteTime
        Signature     = $signature
        Category      = $category
    }
}

$desktopRoot = "C:\Users\tes\OneDrive\Escritorio\CATGIS DESKTOP"
$barriosFolder = Get-ChildItem -LiteralPath $desktopRoot -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like "2 - DCCION. GRAL. DE CATASTRO*Enero 2025" } |
    Select-Object -First 1 -ExpandProperty FullName

$sourceMap = [ordered]@{
    "barrios_dwg" = $barriosFolder
    "kmz"         = (Join-Path $desktopRoot "KMZ")
    "pac"         = (Join-Path $desktopRoot "PAC - Ejido Comodoro Rivadavia - Enero 2025.dwg")
    "pac_backup"  = (Join-Path $desktopRoot "PAC - Ejido Comodoro Rivadavia - Enero 2025.bak")
}

$rows = foreach ($entry in $sourceMap.GetEnumerator()) {
    Get-SourceRows -GroupName $entry.Key -LiteralPath $entry.Value
}

$outputDir = Split-Path -Parent $OutputCsv
if (-not (Test-Path -LiteralPath $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

$rows |
    Sort-Object SourceGroup, FullName |
    Export-Csv -LiteralPath $OutputCsv -NoTypeInformation -Encoding UTF8

Write-Host "Inventario exportado a: $OutputCsv"
Write-Host "Total de registros: $($rows.Count)"

$rows |
    Group-Object SourceGroup, Category |
    Sort-Object Count -Descending |
    Select-Object Count, Name |
    Format-Table -AutoSize
