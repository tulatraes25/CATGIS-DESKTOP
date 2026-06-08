$mapPanelFile = "src/ar/com/catgis/MapPanel.java"
$extractedFiles = @(
    "src/ar/com/catgis/DrawingTools.java",
    "src/ar/com/catgis/KeyboardConfig.java",
    "src/ar/com/catgis/MapInteractionHandler.java",
    "src/ar/com/catgis/MapPopupHandler.java",
    "src/ar/com/catgis/MapRenderer.java"
)

# Get all panel.fieldOrMethod() references from extracted classes
$refs = @{}
foreach ($f in $extractedFiles) {
    $content = Get-Content $f -Raw
    # Match panel.something
    $matches = [regex]::Matches($content, 'panel\.(\w+)')
    foreach ($m in $matches) {
        $refs[$m.Groups[1].Value] = $true
    }
}

$refNames = $refs.Keys | Sort-Object
Write-Host "=== References: $($refNames.Count) unique ==="
$refNames | ForEach-Object { Write-Host "  $_" }

# Now find private declarations in MapPanel matching these names
$mapContent = Get-Content $mapPanelFile -Raw

$changed = 0
foreach ($name in $refNames) {
    # Search for: private <type> <name> or private <returnType> <name>(
    $pattern = "(?m)^(    private (?:static |final |synchronized )*(?:[\w<>,\[\]]+\s+)+)" + [regex]::Escape($name) + "(\s*[(=;])"
    $replacement = '$1' + $name + '$2'
    
    if ($mapContent -match $pattern) {
        # Remove 'private ' from the matched group
        $fixed = $mapContent -replace $pattern, { 
            $_.Value -replace '^    private ', '    '
        }
        if ($fixed -ne $mapContent) {
            $changed++
            Write-Host "  FIXED: $name"
            $mapContent = $fixed
        }
    }
}

Write-Host "=== Changed: $changed ==="
Set-Content $mapPanelFile -Value $mapContent
Write-Host "Done"
