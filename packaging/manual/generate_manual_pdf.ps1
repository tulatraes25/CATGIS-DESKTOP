param(
    [string]$HtmlPath = "C:\CATGIS\catgis-desktop\src\help\CATGIS_Manual_Integrado_2026.html",
    [string]$PdfPath = "C:\CATGIS\catgis-desktop\packaging\manual\CATGIS_Manual_de_Usuario_Actualizado_2026.pdf"
)

$browserCandidates = @(
    "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
    "C:\Program Files\Microsoft\Edge\Application\msedge.exe",
    "C:\Program Files\Google\Chrome\Application\chrome.exe"
)

$browser = $browserCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $browser) {
    throw "No se encontro Microsoft Edge ni Google Chrome para generar el PDF."
}

$resolvedHtml = (Resolve-Path $HtmlPath).Path
$pdfDirectory = Split-Path -Path $PdfPath -Parent
if (-not (Test-Path $pdfDirectory)) {
    New-Item -ItemType Directory -Path $pdfDirectory -Force | Out-Null
}

$resolvedPdf = [System.IO.Path]::GetFullPath($PdfPath)
$userDataDir = Join-Path $env:TEMP "catgis-manual-headless"
Remove-Item -Recurse -Force $userDataDir -ErrorAction SilentlyContinue
$fileUri = "file:///" + ($resolvedHtml -replace "\\", "/")
$arguments = @(
    "--headless=new",
    "--disable-gpu",
    "--no-first-run",
    "--disable-extensions",
    "--allow-file-access-from-files",
    "--user-data-dir=$userDataDir",
    "--print-to-pdf=$resolvedPdf",
    $fileUri
)

$process = Start-Process -FilePath $browser -ArgumentList $arguments -Wait -PassThru -WindowStyle Hidden
if ($null -ne $process.ExitCode -and $process.ExitCode -ne 0) {
    throw "La generacion PDF fallo con codigo $($process.ExitCode)."
}

if (-not (Test-Path $resolvedPdf)) {
    throw "No se genero el PDF esperado en $resolvedPdf."
}

Get-Item $resolvedPdf | Select-Object Name, Length, LastWriteTime, FullName
