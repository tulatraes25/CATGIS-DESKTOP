param(
    [Parameter(Mandatory = $true)]
    [string]$SourceRoot,

    [string]$Database = "catserver",
    [string]$User = "postgres",
    [string]$Password = "",
    [string]$PsqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe",
    [string]$Shp2PgsqlPath = "C:\Program Files\PostgreSQL\18\bin\shp2pgsql.exe",
    [int]$Srid = 4326,
    [string]$Encoding = "LATIN1",
    [switch]$AllowDestructive
)

. (Join-Path $PSScriptRoot "_common\DestructiveGuard.ps1")

function Invoke-PsqlCommand {
    param(
        [string]$Sql
    )

    & $PsqlPath -U $User -d $Database -v ON_ERROR_STOP=1 -c $Sql | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Fallo psql ejecutando SQL."
    }
}

function Import-Category {
    param(
        [string]$Suffix,
        [string]$TargetTable
    )

    $files = Get-ChildItem -Path $SourceRoot -Recurse -Filter ("*_" + $Suffix + "_4326.shp") -File | Sort-Object FullName
    if (-not $files) {
        return [PSCustomObject]@{ Suffix = $Suffix; Count = 0; Table = $TargetTable }
    }

    # DC-8: reescritura con stage+swap. Se importa a una tabla _stage y el
    # reemplazo se hace en una transaccion (rollback conserva la tabla previa).
    $stageTable = $TargetTable + "_stage"
    Clear-CatserverStaleBackups -PathPattern (Join-Path $SourceRoot "*.bak-*") -AllowDestructive:$AllowDestructive
    Invoke-PsqlCommand "DROP TABLE IF EXISTS $stageTable;"

    $createFile = $files[0]
    & $Shp2PgsqlPath -c -I -s $Srid -W $Encoding -- $createFile.FullName $stageTable | & $PsqlPath -U $User -d $Database -v ON_ERROR_STOP=1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Fallo creando $stageTable desde $($createFile.Name)"
    }

    Invoke-PsqlCommand @"
ALTER TABLE $stageTable
    ADD COLUMN IF NOT EXISTS source_file text,
    ADD COLUMN IF NOT EXISTS barrio_key text,
    ADD COLUMN IF NOT EXISTS export_group text,
    ADD COLUMN IF NOT EXISTS imported_at timestamptz NOT NULL DEFAULT now();
"@

    foreach ($file in $files) {
        if ($file.FullName -ne $createFile.FullName) {
            & $Shp2PgsqlPath -a -s $Srid -W $Encoding -- $file.FullName $stageTable | & $PsqlPath -U $User -d $Database -v ON_ERROR_STOP=1 | Out-Null
            if ($LASTEXITCODE -ne 0) {
                throw "Fallo importando $($file.Name) en $stageTable"
            }
        }

        $barrioKey = [System.IO.Path]::GetFileNameWithoutExtension($file.Name) -replace ('_' + $Suffix + '_4326$'), ''
        $safeFile = $file.FullName.Replace("'", "''")
        $safeBarrio = $barrioKey.Replace("'", "''")
        $safeGroup = $Suffix.Replace("'", "''")

        Invoke-PsqlCommand @"
UPDATE $stageTable
SET
    source_file = '$safeFile',
    barrio_key = '$safeBarrio',
    export_group = '$safeGroup'
WHERE source_file IS NULL;
"@
    }

    $targetLeaf = ($TargetTable -split '\.')[-1]
    Invoke-PsqlCommand ("BEGIN; DROP TABLE IF EXISTS $TargetTable; " +
        "ALTER TABLE $stageTable RENAME TO $targetLeaf; COMMIT;")

    return [PSCustomObject]@{ Suffix = $Suffix; Count = $files.Count; Table = $TargetTable }
}

if (-not (Test-Path -LiteralPath $SourceRoot)) {
    throw "No existe la carpeta fuente: $SourceRoot"
}

if (-not (Test-Path -LiteralPath $PsqlPath)) {
    throw "No se encontro psql en: $PsqlPath"
}

if (-not (Test-Path -LiteralPath $Shp2PgsqlPath)) {
    throw "No se encontro shp2pgsql en: $Shp2PgsqlPath"
}

$previousPassword = $env:PGPASSWORD
if ($Password) {
    $env:PGPASSWORD = $Password
}

try {
    Invoke-PsqlCommand "CREATE SCHEMA IF NOT EXISTS staging;"

    $results = @()
    $results += Import-Category -Suffix "lineas" -TargetTable "staging.cad_barrios_lineas_src"
    $results += Import-Category -Suffix "rotulos" -TargetTable "staging.cad_barrios_rotulos_src"
    $results += Import-Category -Suffix "control" -TargetTable "staging.cad_barrios_control_src"

    $results | Format-Table -AutoSize
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
