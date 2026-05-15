param(
    [string]$SourcePath = "C:\CATGIS\catserver_tmp\Drenaje_Comodoro_Rivadavia.kml",
    [string]$Database = "catserver",
    [string]$User = "postgres",
    [string]$Password = "",
    [string]$Schema = "raw",
    [string]$ImportScript = "C:\CATGIS\CATSERVER\scripts\06_import_kml_via_pgdump.ps1"
)

if (-not (Test-Path -LiteralPath $SourcePath)) {
    throw "No existe el KML fuente: $SourcePath"
}

if (-not (Test-Path -LiteralPath $ImportScript)) {
    throw "No existe el importador base: $ImportScript"
}

$layers = @(
    @{ Layer = "Perdido"; Target = "drenaje_perdido_kmz" },
    @{ Layer = "Mosconi"; Target = "drenaje_mosconi_kmz" },
    @{ Layer = "Quintana"; Target = "drenaje_quintana_kmz" },
    @{ Layer = "Cursos Belgrano"; Target = "drenaje_cursos_belgrano_kmz" },
    @{ Layer = "Ramon Santos Norte"; Target = "drenaje_ramon_santos_norte_kmz" },
    @{ Layer = "Cursos RS sur"; Target = "drenaje_cursos_rs_sur_kmz" },
    @{ Layer = "Cursos RT"; Target = "drenaje_cursos_rt_kmz" },
    @{ Layer = "Cursos Km 8"; Target = "drenaje_cursos_km8_kmz" },
    @{ Layer = "cursos principales"; Target = "drenaje_cursos_principales_kmz" },
    @{ Layer = "Cuencas Principales"; Target = "drenaje_cuencas_principales_kmz" },
    @{ Layer = "Norte"; Target = "drenaje_cuencas_norte_kmz" },
    @{ Layer = "Centro - Belgrano"; Target = "drenaje_cuencas_centro_belgrano_kmz" },
    @{ Layer = "Arroyo La Mata"; Target = "drenaje_cuencas_arroyo_lamata_kmz" },
    @{ Layer = "Sur"; Target = "drenaje_cuencas_sur_kmz" },
    @{ Layer = "puentes y alcantarillas"; Target = "drenaje_puentes_alcantarillas_kmz" },
    @{ Layer = "Drenaje Comodoro Rivadavia"; Target = "drenaje_etiquetas_kmz" }
)

$results = foreach ($item in $layers) {
    & $ImportScript `
        -SourcePath $SourcePath `
        -LayerName $item.Layer `
        -Schema $Schema `
        -Table $item.Target `
        -Database $Database `
        -User $User `
        -Password $Password

    if ($LASTEXITCODE -ne 0) {
        throw "Fallo importando la capa $($item.Layer)"
    }

    [PSCustomObject]@{
        Layer  = $item.Layer
        Target = "$Schema.$($item.Target)"
        Status = "OK"
    }
}

$results | Format-Table -AutoSize
