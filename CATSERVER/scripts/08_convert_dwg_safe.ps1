param(
    [Parameter(Mandatory = $true)]
    [string]$SourceDwg,

    [Parameter(Mandatory = $true)]
    [string]$OutputDir,

    [string]$OdaPath = "C:\CATGIS\tools\oda\app\ODAFileConverter.exe",
    [string]$WorkRoot = "C:\CATGIS\catserver_cad\safe_convert",
    [string]$OutputVersion = "ACAD2018",
    [string]$OutputType = "DXF"
)

if (-not (Test-Path -LiteralPath $SourceDwg)) {
    throw "No existe el DWG fuente: $SourceDwg"
}

if (-not (Test-Path -LiteralPath $OdaPath)) {
    throw "No se encontro ODAFileConverter en: $OdaPath"
}

$sourceFile = Get-Item -LiteralPath $SourceDwg
$safeBase = [System.IO.Path]::GetFileNameWithoutExtension($sourceFile.Name) -replace '[^A-Za-z0-9_-]+', '_'
if ([string]::IsNullOrWhiteSpace($safeBase)) {
    $safeBase = "cad"
}

$jobToken = "{0}_{1}" -f $safeBase, ([Math]::Abs($sourceFile.FullName.ToLowerInvariant().GetHashCode()))
$jobIn = Join-Path $WorkRoot ($jobToken + "_in")
$jobOut = Join-Path $WorkRoot ($jobToken + "_out")

New-Item -ItemType Directory -Force -Path $jobIn,$jobOut,$OutputDir | Out-Null

$safeSource = Join-Path $jobIn "cad_input$($sourceFile.Extension.ToLowerInvariant())"
Copy-Item -LiteralPath $sourceFile.FullName -Destination $safeSource -Force

$p = Start-Process `
    -FilePath $OdaPath `
    -ArgumentList @($jobIn, $jobOut, $OutputVersion, $OutputType, "0", "0", [System.IO.Path]::GetFileName($safeSource)) `
    -Wait `
    -PassThru

if ($p.ExitCode -ne 0) {
    throw "ODAFileConverter devolvio ExitCode=$($p.ExitCode)"
}

$expectedExt = "." + $OutputType.ToLowerInvariant()
$generated = Get-ChildItem -LiteralPath $jobOut -Filter ("*" + $expectedExt) | Select-Object -First 1
if (-not $generated) {
    throw "ODAFileConverter no genero el archivo esperado en $jobOut"
}

$finalName = [System.IO.Path]::GetFileNameWithoutExtension($sourceFile.Name) + $expectedExt
$finalPath = Join-Path $OutputDir $finalName
Copy-Item -LiteralPath $generated.FullName -Destination $finalPath -Force

Write-Host "Conversion completada:"
Write-Host $finalPath
