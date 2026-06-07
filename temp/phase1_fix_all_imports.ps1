# Phase 1.5: Bulk add missing imports to ALL files
$BASE = "C:\CATGIS\catgis-desktop\src\ar\com\catgis"

# New imports needed by flat package files (classes that moved OUT)
$NEW_IMPORTS = @(
    @("Layer","import ar.com.catgis.core.model.Layer;"),
    @("Project","import ar.com.catgis.core.model.Project;"),
    @("LayerGroup","import ar.com.catgis.core.model.LayerGroup;"),
    @("LabelConfig","import ar.com.catgis.core.model.LabelConfig;"),
    @("GradientFill","import ar.com.catgis.core.model.GradientFill;"),
    @("ShapefileData","import ar.com.catgis.data.vector.ShapefileData;"),
    @("VectorLayerUtils","import ar.com.catgis.data.vector.VectorLayerUtils;"),
    @("LocalRasterData","import ar.com.catgis.data.raster.LocalRasterData;"),
    @("RasterCoverageSupport","import ar.com.catgis.data.raster.RasterCoverageSupport;"),
    @("OnlineRasterSource","import ar.com.catgis.data.online.OnlineRasterSource;"),
    @("OnlineWmsLayer","import ar.com.catgis.data.online.OnlineWmsLayer;"),
    @("OnlineTileCache","import ar.com.catgis.data.online.OnlineTileCache;"),
    @("GeoprocessingAssistantDialog","import ar.com.catgis.analysis.vector.GeoprocessingAssistantDialog;"),
    @("RasterCalculatorEngine","import ar.com.catgis.analysis.raster.RasterCalculatorEngine;"),
    @("RasterCalculatorDialog","import ar.com.catgis.analysis.raster.RasterCalculatorDialog;"),
    @("PolygonSymbolRenderer","import ar.com.catgis.renderer.PolygonSymbolRenderer;"),
    @("LineSymbolRenderer","import ar.com.catgis.renderer.LineSymbolRenderer;"),
    @("MapDecorationRenderer","import ar.com.catgis.renderer.MapDecorationRenderer;"),
    @("FeatureDecoratorRenderer","import ar.com.catgis.renderer.decorations.FeatureDecoratorRenderer;"),
    @("LabelExpressionEngine","import ar.com.catgis.renderer.labels.LabelExpressionEngine;"),
    @("LabelPlacementEngine","import ar.com.catgis.renderer.labels.LabelPlacementEngine;"),
    @("ToolboxRegistry","import ar.com.catgis.service.ToolboxRegistry;"),
    @("ToolboxAlgorithm","import ar.com.catgis.service.ToolboxAlgorithm;")
)

$files = Get-ChildItem $BASE -Recurse -Filter "*.java"
$modCount = 0
$impCount = 0

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $orig = $content
    $fileName = $file.Name
    
    foreach ($entry in $NEW_IMPORTS) {
        $cls = $entry[0]
        $importLine = $entry[1]
        
        # Skip if this IS the class file
        if ($fileName -eq "$cls.java") { continue }
        # Skip if already imported
        if ($content -match [regex]::Escape($importLine)) { continue }
        
        # Check if class name appears in the code (not just in comments/strings)
        # Use a simple word-boundary check
        $pattern = "(?<=[^.a-zA-Z_0-9])$cls(?=[^a-zA-Z_0-9])"
        if ($content -match $pattern) {
            # Add import after the last import or after package
            $content = $content -replace '(package .+?;\r?\n)((?:import .+?;\r?\n)*)', "`$1`$2$importLine`r`n"
            $impCount++
        }
    }
    
    if ($content -ne $orig) {
        Set-Content $file.FullName $content -Force -NoNewline
        $modCount++
    }
}

Write-Output "Files modified: $modCount"
Write-Output "Imports added: $impCount"
