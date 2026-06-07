# Phase 1.4: Fix imports in MOVED files (they now need imports for flat package classes)
$BASE = "C:\CATGIS\catgis-desktop\src\ar\com\catgis"

# Get all classes still in the flat package (ar.com.catgis)
$flatClasses = Get-ChildItem $BASE -Filter "*.java" -Depth 0 | ForEach-Object { $_.BaseName }
Write-Host "Flat package has $($flatClasses.Count) classes"

# Get all moved files (in subpackages)
$movedFiles = Get-ChildItem $BASE -Recurse -Filter "*.java" | Where-Object {
    $_.FullName -match '\\(core|data|analysis|renderer|service|ui|util)\\.*\.java$'
}

$importsAdded = 0
$filesModified = 0

foreach ($file in $movedFiles) {
    $content = Get-Content $file.FullName -Raw
    $original = $content
    $fileName = $file.Name
    
    foreach ($cls in $flatClasses) {
        # Skip if this IS the class or if already imported
        if ($fileName -eq "$cls.java") { continue }
        if ($content -match "import ar\.com\.catgis\.$cls;") { continue }
        
        # Check if the class name appears in the file as a type reference
        if ($content -match "(?<=[^a-zA-Z_])$cls(?=[^a-zA-Z_])") {
            $importLine = "import ar.com.catgis.$cls;"
            # Add after package declaration, before other imports
            $content = $content -replace '(package ar\.com\.catgis\.[a-z.]+;\r?\n)(?!import)',
                "`$1`r`n$importLine"
            if ($content -eq $original) {
                # Try after last existing import
                $content = $content -replace '^(package .+?;\r?\n)((?:import .+?;\r?\n)*)',
                    "`$1`$2$importLine`r`n"
            }
            $importsAdded++
        }
    }
    
    if ($content -ne $original) {
        Set-Content $file.FullName $content -Force -NoNewline
        $filesModified++
    }
}

Write-Host "Files modified: $filesModified"
Write-Host "Imports added: $importsAdded"
