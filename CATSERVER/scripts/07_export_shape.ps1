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
    [string]$Pgsql2ShpPath = "C:\Program Files\PostgreSQL\18\bin\pgsql2shp.exe"
)

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
$existing = Join-Path $targetDir ($baseName + ".*")
Get-ChildItem -Path $existing -ErrorAction SilentlyContinue | Remove-Item -Force

$previousPassword = $env:PGPASSWORD
if ($Password) {
    $env:PGPASSWORD = $Password
}

try {
    $args = @(
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
        $args += $Table
    }
    else {
        $args += $Query
    }

    & $Pgsql2ShpPath @args

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo pgsql2shp al exportar a $OutputPath"
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
