[CmdletBinding()]
param(
    [string]$RepositoryUrl = "https://github.com/tulatraes25/CATGIS-DESKTOP.git",
    [string]$WorkRoot = (Join-Path $PWD "CATGIS_HISTORY_SANITIZE"),
    [string]$HistoricalSourceRoot = "",
    [string]$ReplacementSourceRoot = "<SOURCE_ROOT>",
    [string]$HistoricalAuthorEmail = "",
    [string]$ReplacementAuthorName = "tulatraes25",
    [string]$ReplacementAuthorEmail = "272926918+tulatraes25@users.noreply.github.com",
    [switch]$PushRewrittenHistory
)

$ErrorActionPreference = "Stop"

function Run-Git {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
    & git @Args
    if ($LASTEXITCODE -ne 0) {
        throw "git command failed: $($Args -join ' ')"
    }
}

function Find-GitPickaxeMatches {
    param([Parameter(Mandatory = $true)][string]$Needle)

    if ([string]::IsNullOrEmpty($Needle)) {
        throw "El literal de verificacion git -S no puede estar vacio."
    }

    # PowerShell 5.1 must hand git one native argument even when the literal contains spaces.
    $pickaxeArgument = "-S$Needle"
    $matches = @(& git log --all --oneline $pickaxeArgument)
    if ($LASTEXITCODE -ne 0) {
        throw "git pickaxe verification failed for supplied literal."
    }

    return $matches
}

function New-FilterRepoLiteralRule {
    param(
        [Parameter(Mandatory = $true)][string]$Source,
        [Parameter(Mandatory = $true)][string]$Replacement
    )

    if ([string]::IsNullOrWhiteSpace($Source)) {
        throw "La fuente de una regla --replace-text no puede estar vacia."
    }
    if ($Source.IndexOf([char]13) -ge 0 -or $Source.IndexOf([char]10) -ge 0 -or
        $Replacement.IndexOf([char]13) -ge 0 -or $Replacement.IndexOf([char]10) -ge 0) {
        throw "Las reglas --replace-text no pueden contener saltos de linea."
    }
    if ($Source.Contains("==>") -or $Replacement.Contains("==>")) {
        throw "Source/Replacement no pueden contener el delimitador reservado ==>"
    }

    return "literal:$Source==>$Replacement"
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "Git no esta disponible."
}

& git filter-repo --version *> $null
if ($LASTEXITCODE -ne 0) {
    throw "Falta git-filter-repo."
}

if (Test-Path -LiteralPath $WorkRoot) {
    $items = @(Get-ChildItem -LiteralPath $WorkRoot -Force -ErrorAction SilentlyContinue)
    if ($items.Count -gt 0) {
        throw "WorkRoot no esta vacio. Usar una carpeta nueva y desechable: $WorkRoot"
    }
} else {
    New-Item -ItemType Directory -Path $WorkRoot -Force | Out-Null
}

$mirrorPath = Join-Path $WorkRoot "CATGIS-DESKTOP-sanitize.git"
$refsPath = Join-Path $WorkRoot "CATGIS_PRE_REWRITE_REFS.txt"
$replacementPath = Join-Path $WorkRoot "catgis-replacements.txt"
$mailmapPath = Join-Path $WorkRoot "catgis-mailmap.txt"

Write-Host "[1/8] Clon mirror fresco"
Run-Git clone --mirror $RepositoryUrl $mirrorPath

Push-Location $mirrorPath
try {
    Write-Host "[2/8] Guardando refs e integridad original"
    (& git show-ref) | Set-Content -LiteralPath $refsPath -Encoding UTF8
    Run-Git fsck --full

    Write-Host "[3/8] Eliminando paths no publicables"
    Run-Git filter-repo --force --path "CATSERVER/docs/07_datos_conexion_catserver.txt" --path "kosmo_decompilado" --path-glob "CATSERVER/inventory/*.csv" --invert-paths

    if (-not [string]::IsNullOrWhiteSpace($HistoricalSourceRoot)) {
        $historicalSourceRootForward = $HistoricalSourceRoot -replace '\\','/'
        $replacementRules = @(
            (New-FilterRepoLiteralRule -Source $HistoricalSourceRoot -Replacement $ReplacementSourceRoot)
            (New-FilterRepoLiteralRule -Source $historicalSourceRootForward -Replacement $ReplacementSourceRoot)
        ) | Select-Object -Unique

        $invalidRules = @($replacementRules | Where-Object { $_ -notmatch '==>' })
        if ($invalidRules.Count -gt 0) {
            throw "Regla --replace-text invalida: falta el delimitador requerido ==>"
        }

        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllLines($replacementPath, [string[]]$replacementRules, $utf8NoBom)

        $writtenRules = [System.IO.File]::ReadAllLines($replacementPath, $utf8NoBom)
        if ($writtenRules.Count -ne $replacementRules.Count -or
            @($writtenRules | Where-Object { $_ -notmatch '==>' }).Count -gt 0) {
            throw "No se pudo validar el archivo local de reglas --replace-text."
        }

        Write-Host "[4/8] Anonimizando rutas historicas"
        Run-Git filter-repo --force --replace-text $replacementPath
    } else {
        Write-Host "[4/8] Sin HistoricalSourceRoot: se omite anonimizado de ruta privada."
    }

    if (-not [string]::IsNullOrWhiteSpace($HistoricalAuthorEmail)) {
        Write-Host "[4b/8] Anonimizando email historico de autoria"
        "$ReplacementAuthorName <$ReplacementAuthorEmail> <$HistoricalAuthorEmail>" | Set-Content -LiteralPath $mailmapPath -Encoding UTF8
        Run-Git filter-repo --force --mailmap $mailmapPath
    }

    Write-Host "[5/8] Verificando ausencia historica"
    $bad = @()

    if (& git log --all -- "CATSERVER/docs/07_datos_conexion_catserver.txt") {
        $bad += "credential-file-history"
    }
    if (& git log --all -- "kosmo_decompilado") {
        $bad += "kosmo-history"
    }
    if (& git log --all -- "CATSERVER/inventory/*.csv") {
        $bad += "inventory-csv-history"
    }
    if (-not [string]::IsNullOrWhiteSpace($HistoricalSourceRoot)) {
        $historicalSourceRootForward = $HistoricalSourceRoot -replace '\\','/'
        $backslashMatches = @(Find-GitPickaxeMatches -Needle $HistoricalSourceRoot)
        if ($backslashMatches.Count -gt 0) {
            $bad += "workstation-path-backslash-history"
        }
        $forwardslashMatches = @(Find-GitPickaxeMatches -Needle $historicalSourceRootForward)
        if ($forwardslashMatches.Count -gt 0) {
            $bad += "workstation-path-forwardslash-history"
        }
    }
    if (-not [string]::IsNullOrWhiteSpace($HistoricalAuthorEmail)) {
        $emailMatches = & git log --all --format="%ae%n%ce" | Select-String -SimpleMatch $HistoricalAuthorEmail
        if ($emailMatches) { $bad += "historical-author-email" }
    }

    if ($bad.Count -gt 0) {
        throw "Verificacion historica fallo: $($bad -join ', ')"
    }

    Write-Host "[6/8] Verificando arbol canonico"
    foreach ($required in @("catgis-desktop","CATSERVER","catserver-web","docs","README.md","SECURITY.md")) {
        & git cat-file -e "refs/heads/main:$required" 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Falta despues del rewrite: $required"
        }
    }
    Run-Git fsck --full

    Write-Host "[7/8] Restaurando remote origin"
    $remotes = @(& git remote)
    if ($remotes -contains "origin") {
        Run-Git remote remove origin
    }
    Run-Git remote add origin $RepositoryUrl

    if (-not $PushRewrittenHistory) {
        Write-Host ""
        Write-Host "VALIDACION LOCAL COMPLETA. No se hizo push."
        Write-Host "Mirror reescrito: $mirrorPath"
        Write-Host "Refs originales: $refsPath"
        Write-Host "Opcional: pasar -HistoricalSourceRoot y -HistoricalAuthorEmail de forma local para anonimizar datos historicos sin publicarlos en el repo."
        Write-Host "Repetir con -PushRewrittenHistory solo durante una ventana controlada."
        return
    }

    Write-Host "[8/8] PUBLICANDO HISTORIA REESCRITA"
    Write-Warning "Esta operacion cambia los SHA historicos del repositorio remoto."
    Run-Git push --force origin --all
    Run-Git push --force origin --tags

    Write-Host "HISTORY_REWRITE_PUSH_COMPLETE"
    Write-Host "Todos los clones anteriores deben descartarse o re-clonarse."
}
finally {
    Pop-Location
}
