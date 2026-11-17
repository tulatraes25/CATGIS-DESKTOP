param(
    [Parameter(Mandatory = $true)]
    [string]$OutputPath,

    [string]$Database = "catserver",
    [string]$DbHost = "localhost",
    [int]$Port = 5432,
    [string]$User = "postgres",
    [string]$Password = "",
    [string]$GeometryColumn = "geom",
    [string]$Table = "",
    [string]$Query = "",
    [string]$Pgsql2ShpPath = "C:\Program Files\PostgreSQL\18\bin\pgsql2shp.exe",
    [switch]$AllowDestructive
)

. (Join-Path $PSScriptRoot "_common\DestructiveGuard.ps1")

if (-not (Test-Path -LiteralPath $Pgsql2ShpPath)) {
    throw "No se encontro pgsql2shp en: $Pgsql2ShpPath"
}

if ([string]::IsNullOrWhiteSpace($Table) -and [string]::IsNullOrWhiteSpace($Query)) {
    throw "Debes indicar -Table o -Query"
}

if (-not [string]::IsNullOrWhiteSpace($Table) -and -not [string]::IsNullOrWhiteSpace($Query)) {
    throw "Usa solo una opcion: -Table o -Query"
}

$targetDir = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

$baseName = [System.IO.Path]::GetFileNameWithoutExtension($OutputPath)
Clear-CatserverStaleBackups -PathPattern (Join-Path $targetDir ($baseName + ".*.bak-*")) -AllowDestructive:$AllowDestructive
$backups = @()
Get-ChildItem -Path (Join-Path $targetDir ($baseName + ".*")) -ErrorAction SilentlyContinue |
    ForEach-Object {
        $backup = Move-CatserverToBackup -Path $_.FullName
        if ($backup) { $backups += $backup }
    }

$previousPassword = $env:PGPASSWORD
if ($Password) {
    $env:PGPASSWORD = $Password
}

try {
    $pgsql2shpArgs = @(
        "-f", $OutputPath,
        "-h", $DbHost,
        "-p", $Port,
        "-u", $User,
        "-P", $Password,
        "-g", $GeometryColumn,
        "-r",
        $Database
    )

    if (-not [string]::IsNullOrWhiteSpace($Table)) {
        $pgsql2shpArgs += $Table
    }
    else {
        $pgsql2shpArgs += $Query
    }

    try {
        & $Pgsql2ShpPath @pgsql2shpArgs
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo pgsql2shp al exportar a $OutputPath"
        }
    }
    catch {
        Get-ChildItem -Path (Join-Path $targetDir ($baseName + ".*")) -ErrorAction SilentlyContinue |
            Where-Object { $backups -notcontains $_.FullName } |
            ForEach-Object { Remove-CatserverTransientBackup -Path $_.FullName }
        foreach ($backup in $backups) {
            $original = $backup -replace '\.bak-[0-9a-f]{8}$', ''
            Restore-CatserverBackup -BackupPath $backup -OriginalPath $original
        }
        throw
    }
    foreach ($backup in $backups) {
        Remove-CatserverTransientBackup -Path $backup
    }
}
finally {
    if ($Password) {
        if ($null -ne $previousPassword) {
            $env:PGPASSWORD = $previousPassword
        }
        else {
            Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
        }
    }
}

Write-Host "Exportacion shape completada: $OutputPath"
