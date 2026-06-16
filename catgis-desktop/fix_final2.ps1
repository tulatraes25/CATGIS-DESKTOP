$base = 'C:\CATGIS\catgis-desktop\src\ar\com\catgis'
$totalInner = 0
$totalFiles = 0
foreach ($file in Get-ChildItem $base -Recurse -Filter *.java) {
    $lines = [IO.File]::ReadAllLines($file.FullName)
    $out = New-Object System.Collections.Generic.List[string]
    $i = 0
    $fixed = $false
    while ($i -lt $lines.Count) {
        $l = $lines[$i]
        $m = [regex]::Match($l, '^(\s*)if\s*\(\s*CatgisDesktopApp\.(layersPanel|statusBar)\s*!=\s*null\s*\)\s*\{\s*$')
        if ($m.Success) {
            $indent = $m.Groups[1].Value
            $inner = @()
            $j = $i + 1
            $foundClose = $false
            while ($j -lt $lines.Count) {
                $il = $lines[$j]
                if ($il.Trim() -eq '}') { $foundClose = $true; break }
                $inner += $il
                $j++
            }
            if ($foundClose -and $inner.Count -ge 1) {
                $ok = $true
                foreach ($il in $inner) {
                    $t = $il.TrimStart()
                    if ($t -notmatch '^AppContext\.' -and $t -notmatch '^CatgisDesktopApp\.syncFloating') { $ok = $false; break }
                }
                if ($ok) {
                    foreach ($il in $inner) { $out.Add($indent + $il.TrimStart()) }
                    $fixed = $true
                    $totalInner += $inner.Count
                    $i = $j + 1
                    continue
                }
            }
        }
        $out.Add($l)
        $i++
    }
    if ($fixed) {
        [IO.File]::WriteAllLines($file.FullName, $out)
        $totalFiles++
        Write-Host $file.Name
    }
}
Write-Host "Files: $totalFiles, Inner lines moved: $totalInner"
