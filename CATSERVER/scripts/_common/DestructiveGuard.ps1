# CATSERVER destructive-path guard (DC-8 / T4)
#
# Politica decidida por el owner (opcion A):
#   - Las reescrituras de artefactos usan stage/backup + swap: el artefacto
#     anterior se mueve a un backup temporal y se restaura si la operacion falla.
#   - Los borrados reales de datos del operador requieren -AllowDestructive.
#
# Los scripts 07/10/12/14/16 importan este archivo y declaran el switch.

function New-CatserverBackupPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )
    return ($Path + ".bak-" + [guid]::NewGuid().ToString('N').Substring(0, 8))
}

function Move-CatserverToBackup {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )
    if (-not (Test-Path -LiteralPath $Path)) {
        return $null
    }
    $backup = New-CatserverBackupPath -Path $Path
    Move-Item -LiteralPath $Path -Destination $backup
    return $backup
}

function Remove-CatserverTransientBackup {
    param(
        [string]$Path
    )
    if ([string]::IsNullOrWhiteSpace($Path)) {
        return
    }
    if (Test-Path -LiteralPath $Path) {
        Remove-Item -LiteralPath $Path -Recurse -Force
    }
}

function Restore-CatserverBackup {
    param(
        [string]$BackupPath,
        [Parameter(Mandatory = $true)]
        [string]$OriginalPath
    )
    if ([string]::IsNullOrWhiteSpace($BackupPath)) {
        return
    }
    if (Test-Path -LiteralPath $BackupPath) {
        if (Test-Path -LiteralPath $OriginalPath) {
            Remove-Item -LiteralPath $OriginalPath -Recurse -Force
        }
        Move-Item -LiteralPath $BackupPath -Destination $OriginalPath
    }
}

function Assert-CatserverDestructiveAllowed {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Operation,
        [Parameter(Mandatory = $true)]
        [string]$Target,
        [switch]$AllowDestructive
    )
    if (-not $AllowDestructive) {
        throw ("CATSERVER DC-8: '$Operation' sobre '$Target' requiere -AllowDestructive. " +
               "La politica de rutas destructivas exige confirmacion explicita para borrados.")
    }
}

function Clear-CatserverStaleBackups {
    param(
        [Parameter(Mandatory = $true)]
        [string]$PathPattern,
        [switch]$AllowDestructive
    )
    # Los backups temporales de ejecuciones fallidas se limpian solo con el
    # switch explicito: nunca se borra nada de forma silenciosa.
    if (-not $AllowDestructive) {
        return
    }
    Get-ChildItem -Path $PathPattern -Force -ErrorAction SilentlyContinue |
        ForEach-Object { Remove-CatserverTransientBackup -Path $_.FullName }
}
