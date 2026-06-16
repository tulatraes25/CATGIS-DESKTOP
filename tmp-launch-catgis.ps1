param(
  [string]$ExePath,
  [string]$WorkingDir
)
$psi = New-Object System.Diagnostics.ProcessStartInfo
$psi.FileName = $ExePath
$psi.WorkingDirectory = $WorkingDir
$psi.UseShellExecute = $false
[System.Diagnostics.Process]::Start($psi) | Out-Null
