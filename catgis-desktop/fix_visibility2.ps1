$mapPanelFile = "src/ar/com/catgis/MapPanel.java"
$mapContent = Get-Content $mapPanelFile -Raw

# Read all extracted class files
$extractedFiles = @(
    "src/ar/com/catgis/DrawingTools.java",
    "src/ar/com/catgis/KeyboardConfig.java",
    "src/ar/com/catgis/MapInteractionHandler.java",
    "src/ar/com/catgis/MapPopupHandler.java",
    "src/ar/com/catgis/MapRenderer.java"
)

# Collect all panel.XXX references
$refs = @{}
foreach ($f in $extractedFiles) {
    $content = Get-Content $f -Raw
    $matches = [regex]::Matches($content, 'panel\.(\w+)')
    foreach ($m in $matches) { $refs[$m.Groups[1].Value] = $true }
}

# For each reference, find its private declaration in MapPanel and strip 'private'
$changed = 0
foreach ($name in ($refs.Keys | Sort-Object)) {
    # Pattern: line starting with spaces, "private", optional modifiers, type, name
    $pattern = '(?m)^(    private (?:static |final |synchronized |volatile )*(?:[\w<>,\[\]\?]+\s+)+)' + [regex]::Escape($name) + '(\s*[(=;{])'
    
    if ($mapContent -match $pattern) {
        $matchVal = $matches[0]
        # Replace 'private ' (after leading spaces) with nothing
        $replacement = $matchVal -replace '^([ ]+)private ', '$1'
        if ($replacement -ne $matchVal) {
            $mapContent = $mapContent.Replace($matchVal, $replacement)
            $changed++
            Write-Host "  FIXED: $name"
        }
    }
}

Write-Host "Total changed: $changed"
Set-Content $mapPanelFile -Value $mapContent
Write-Host "Done!"
