# Phase 1 refactor: Create package structure
# Run from C:\CATGIS

$BASE = "C:\CATGIS\catgis-desktop\src\ar\com\catgis"
$PACKAGES = @(
    "core\model",
    "core\geometry",
    "core\style",
    "data",
    "data\vector",
    "data\raster",
    "data\online",
    "analysis",
    "analysis\vector",
    "analysis\raster",
    "renderer",
    "renderer\vector",
    "renderer\labels",
    "renderer\decorations",
    "service",
    "ui\main",
    "ui\panels",
    "ui\dialogs",
    "ui\widgets",
    "util"
)

# Create directories
foreach ($pkg in $PACKAGES) {
    $dir = Join-Path $BASE $pkg
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "Created: $dir"
    }
}
Write-Host "`nPackage structure created."
Write-Host "Total packages: " ($PACKAGES.Length + 1) " (main + $($PACKAGES.Length) sub)"
