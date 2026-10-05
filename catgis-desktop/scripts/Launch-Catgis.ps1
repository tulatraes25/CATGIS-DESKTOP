param(
    [Parameter(Mandatory = $true)]
    [string]$JavaExe,
    [Parameter(Mandatory = $true)]
    [string]$WorkingDir,
    [string]$JavaLibraryPath = ""
)

$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = $JavaExe
$psi.WorkingDirectory = $WorkingDir
$psi.UseShellExecute = $false

$arguments = @('-Dfile.encoding=UTF-8')
if ($JavaLibraryPath -and $JavaLibraryPath.Trim().Length -gt 0) {
    $arguments += "-Djava.library.path=$JavaLibraryPath"
}
$arguments += @('-cp', 'app\*', 'ar.com.catgis.Main')

$psi.Arguments = [string]::Join(' ', ($arguments | ForEach-Object {
    if ($_ -match '\s') { '"' + $_ + '"' } else { $_ }
}))

[System.Diagnostics.Process]::Start($psi) | Out-Null
