param(
    [string]$SourceRoot = "C:\CATGIS\catserver_shapes\export",
    [string]$OutputRoot = "C:\CATGIS\catserver_shapes\deliveries",
    [string]$PackageDate = "20260413"
)

$ErrorActionPreference = "Stop"

function Copy-ShapeFamily {
    param(
        [Parameter(Mandatory = $true)]
        [string]$SourceShape,

        [Parameter(Mandatory = $true)]
        [string]$TargetShape
    )

    if (-not (Test-Path -LiteralPath $SourceShape)) {
        throw "No se encontro el shape fuente: $SourceShape"
    }

    $sourceDir = Split-Path -Parent $SourceShape
    $sourceBase = [System.IO.Path]::GetFileNameWithoutExtension($SourceShape)
    $targetDir = Split-Path -Parent $TargetShape
    $targetBase = [System.IO.Path]::GetFileNameWithoutExtension($TargetShape)

    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

    Get-ChildItem -LiteralPath $sourceDir -File |
        Where-Object { $_.BaseName -eq $sourceBase } |
        ForEach-Object {
            $targetFile = Join-Path $targetDir ($targetBase + $_.Extension)
            Copy-Item -LiteralPath $_.FullName -Destination $targetFile -Force
        }
}

$packageName = "CATSERVER_shape_operativo_$PackageDate"
$packageDir = Join-Path $OutputRoot $packageName
$controlDir = Join-Path $packageDir "99_control"
$inventoryDir = Join-Path $controlDir "inventarios"

if (Test-Path -LiteralPath $packageDir) {
    $resolvedOutputRoot = [System.IO.Path]::GetFullPath($OutputRoot)
    $resolvedPackageDir = [System.IO.Path]::GetFullPath($packageDir)

    if (-not $resolvedPackageDir.StartsWith($resolvedOutputRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "La carpeta de salida queda fuera del arbol permitido: $packageDir"
    }

    Get-ChildItem -LiteralPath $packageDir -Force | Remove-Item -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $packageDir, $controlDir, $inventoryDir | Out-Null

$layers = @(
    [pscustomobject]@{
        Group = "01_catastro"
        LayerCode = "catastro_barrios"
        SourceShape = Join-Path $SourceRoot "barrios\barrios_shape_src.shp"
        TargetShape = Join-Path $packageDir "01_catastro\catastro_barrios.shp"
        Scope = "operativo"
        Note = "Barrios municipales desde KMZ."
    },
    [pscustomobject]@{
        Group = "01_catastro"
        LayerCode = "catastro_parcelas"
        SourceShape = Join-Path $SourceRoot "parcelas\parcelas_shape_src.shp"
        TargetShape = Join-Path $packageDir "01_catastro\catastro_parcelas.shp"
        Scope = "operativo"
        Note = "Parcelario desde KMZ."
    },
    [pscustomobject]@{
        Group = "01_catastro"
        LayerCode = "catastro_circsect"
        SourceShape = Join-Path $SourceRoot "circsect\circsect_shape_src.shp"
        TargetShape = Join-Path $packageDir "01_catastro\catastro_circsect.shp"
        Scope = "operativo"
        Note = "Circunscripciones y secciones desde KMZ."
    },
    [pscustomobject]@{
        Group = "01_catastro"
        LayerCode = "catastro_ejido_urbano_mask"
        SourceShape = Join-Path $SourceRoot "ejido\ejido_urbano_mask.shp"
        TargetShape = Join-Path $packageDir "01_catastro\catastro_ejido_urbano_mask.shp"
        Scope = "control"
        Note = "Mascara operativa de ejido urbano generada desde circsect."
    },
    [pscustomobject]@{
        Group = "01_catastro"
        LayerCode = "catastro_cad_barrios_lineas_ejido"
        SourceShape = Join-Path $SourceRoot "cad_consolidado\cad_barrios_lineas_ejido.shp"
        TargetShape = Join-Path $packageDir "01_catastro\catastro_cad_barrios_lineas_ejido.shp"
        Scope = "operativo"
        Note = "Lineas CAD barriales filtradas por ejido."
    },
    [pscustomobject]@{
        Group = "01_catastro"
        LayerCode = "catastro_cad_barrios_rotulos_ejido"
        SourceShape = Join-Path $SourceRoot "cad_consolidado\cad_barrios_rotulos_ejido.shp"
        TargetShape = Join-Path $packageDir "01_catastro\catastro_cad_barrios_rotulos_ejido.shp"
        Scope = "operativo"
        Note = "Rotulos CAD barriales filtrados por ejido."
    },
    [pscustomobject]@{
        Group = "01_catastro"
        LayerCode = "catastro_cad_barrios_control_ejido"
        SourceShape = Join-Path $SourceRoot "cad_consolidado\cad_barrios_control_ejido.shp"
        TargetShape = Join-Path $packageDir "01_catastro\catastro_cad_barrios_control_ejido.shp"
        Scope = "control"
        Note = "Puntos de control CAD barriales filtrados por ejido."
    },
    [pscustomobject]@{
        Group = "02_planeamiento"
        LayerCode = "planeamiento_zonificacion"
        SourceShape = Join-Path $SourceRoot "zonif\zonif_shape_src.shp"
        TargetShape = Join-Path $packageDir "02_planeamiento\planeamiento_zonificacion.shp"
        Scope = "operativo"
        Note = "Zonificacion desde KMZ."
    },
    [pscustomobject]@{
        Group = "03_infraestructura"
        LayerCode = "infraestructura_lineas"
        SourceShape = Join-Path $SourceRoot "lineas\lineas_shape_operativa.shp"
        TargetShape = Join-Path $packageDir "03_infraestructura\infraestructura_lineas.shp"
        Scope = "operativo"
        Note = "Infraestructura lineal depurada para uso operativo."
    },
    [pscustomobject]@{
        Group = "04_hidrologia"
        LayerCode = "hidrologia_reservorios_poligonos"
        SourceShape = Join-Path $SourceRoot "reservorios\reservpol_shape_src.shp"
        TargetShape = Join-Path $packageDir "04_hidrologia\hidrologia_reservorios_poligonos.shp"
        Scope = "operativo"
        Note = "Reservorios en poligonos desde KMZ."
    },
    [pscustomobject]@{
        Group = "04_hidrologia"
        LayerCode = "hidrologia_reservorios_barreras"
        SourceShape = Join-Path $SourceRoot "reservorios\reservbar_shape_src.shp"
        TargetShape = Join-Path $packageDir "04_hidrologia\hidrologia_reservorios_barreras.shp"
        Scope = "operativo"
        Note = "Barreras de reservorios desde KMZ."
    },
    [pscustomobject]@{
        Group = "04_hidrologia"
        LayerCode = "hidrologia_reservorios_nombres"
        SourceShape = Join-Path $SourceRoot "reservorios\reservnomb_shape_src.shp"
        TargetShape = Join-Path $packageDir "04_hidrologia\hidrologia_reservorios_nombres.shp"
        Scope = "operativo"
        Note = "Etiquetas de reservorios desde KMZ."
    },
    [pscustomobject]@{
        Group = "04_hidrologia"
        LayerCode = "hidrologia_drenaje_lineas_ejido"
        SourceShape = Join-Path $SourceRoot "drenaje\drenaje_lineas_ejido.shp"
        TargetShape = Join-Path $packageDir "04_hidrologia\hidrologia_drenaje_lineas_ejido.shp"
        Scope = "operativo"
        Note = "Lineas de drenaje dentro del ejido."
    },
    [pscustomobject]@{
        Group = "04_hidrologia"
        LayerCode = "hidrologia_drenaje_cuencas_principales_ejido"
        SourceShape = Join-Path $SourceRoot "drenaje\drenaje_cuencas_principales_ejido.shp"
        TargetShape = Join-Path $packageDir "04_hidrologia\hidrologia_drenaje_cuencas_principales_ejido.shp"
        Scope = "operativo"
        Note = "Cuencas principales dentro del ejido."
    },
    [pscustomobject]@{
        Group = "04_hidrologia"
        LayerCode = "hidrologia_drenaje_cuencas_regiones_ejido"
        SourceShape = Join-Path $SourceRoot "drenaje\drenaje_cuencas_regiones_ejido.shp"
        TargetShape = Join-Path $packageDir "04_hidrologia\hidrologia_drenaje_cuencas_regiones_ejido.shp"
        Scope = "operativo"
        Note = "Cuencas regionales dentro del ejido."
    },
    [pscustomobject]@{
        Group = "04_hidrologia"
        LayerCode = "hidrologia_drenaje_puentes_ejido"
        SourceShape = Join-Path $SourceRoot "drenaje\drenaje_puentes_ejido.shp"
        TargetShape = Join-Path $packageDir "04_hidrologia\hidrologia_drenaje_puentes_ejido.shp"
        Scope = "operativo"
        Note = "Puentes y alcantarillas dentro del ejido."
    },
    [pscustomobject]@{
        Group = "05_referencia_cad"
        LayerCode = "referencia_pac_circsect_wgs84"
        SourceShape = Join-Path $SourceRoot "cad_pac\circsect_pac_wgs84.shp"
        TargetShape = Join-Path $packageDir "05_referencia_cad\referencia_pac_circsect_wgs84.shp"
        Scope = "referencia"
        Note = "Circsect PAC convertido desde DWG."
    },
    [pscustomobject]@{
        Group = "05_referencia_cad"
        LayerCode = "referencia_pac_limites_wgs84"
        SourceShape = Join-Path $SourceRoot "cad_pac\limites_pac_wgs84.shp"
        TargetShape = Join-Path $packageDir "05_referencia_cad\referencia_pac_limites_wgs84.shp"
        Scope = "referencia"
        Note = "Limites PAC convertidos desde DWG."
    },
    [pscustomobject]@{
        Group = "99_control"
        LayerCode = "control_drenaje_etiquetas_fuente"
        SourceShape = Join-Path $SourceRoot "drenaje\drenaje_etiquetas_src.shp"
        TargetShape = Join-Path $packageDir "99_control\control_drenaje_etiquetas_fuente.shp"
        Scope = "control"
        Note = "Etiquetas fuente de drenaje; no intersectan el ejido."
    }
)

foreach ($layer in $layers) {
    Copy-ShapeFamily -SourceShape $layer.SourceShape -TargetShape $layer.TargetShape
}

$inventoryFiles = @(
    "C:\CATGIS\CATSERVER\inventory\source_inventory_20260413.csv",
    "C:\CATGIS\CATSERVER\inventory\cad_barrios_ejido_summary_20260413.csv",
    "C:\CATGIS\CATSERVER\inventory\drenaje_summary_20260413.csv"
)

foreach ($inventoryFile in $inventoryFiles) {
    if (Test-Path -LiteralPath $inventoryFile) {
        Copy-Item -LiteralPath $inventoryFile -Destination (Join-Path $inventoryDir ([System.IO.Path]::GetFileName($inventoryFile))) -Force
    }
}

$manifest = $layers | Select-Object `
    @{Name = "group"; Expression = { $_.Group }}, `
    @{Name = "layer_code"; Expression = { $_.LayerCode }}, `
    @{Name = "scope"; Expression = { $_.Scope }}, `
    @{Name = "source_shape"; Expression = { $_.SourceShape }}, `
    @{Name = "target_shape"; Expression = { $_.TargetShape }}, `
    @{Name = "note"; Expression = { $_.Note }}

$manifestPath = Join-Path $controlDir "manifest_shape_delivery_$PackageDate.csv"
$manifest | Export-Csv -Path $manifestPath -NoTypeInformation -Encoding UTF8

$catgisLoadOrder = @(
    [pscustomobject]@{
        load_order = 1
        layer_code = "catastro_ejido_urbano_mask"
        target_shape = Join-Path $packageDir "01_catastro\catastro_ejido_urbano_mask.shp"
        default_visible = "no"
        recommendation = "Usar como mascara y control; dejar apagada."
    },
    [pscustomobject]@{
        load_order = 2
        layer_code = "catastro_circsect"
        target_shape = Join-Path $packageDir "01_catastro\catastro_circsect.shp"
        default_visible = "no"
        recommendation = "Marco administrativo de apoyo; prender solo si hace falta."
    },
    [pscustomobject]@{
        load_order = 3
        layer_code = "catastro_barrios"
        target_shape = Join-Path $packageDir "01_catastro\catastro_barrios.shp"
        default_visible = "si"
        recommendation = "Base de referencia barrial."
    },
    [pscustomobject]@{
        load_order = 4
        layer_code = "planeamiento_zonificacion"
        target_shape = Join-Path $packageDir "02_planeamiento\planeamiento_zonificacion.shp"
        default_visible = "si"
        recommendation = "Cargar con transparencia para leer sobre catastro."
    },
    [pscustomobject]@{
        load_order = 5
        layer_code = "catastro_parcelas"
        target_shape = Join-Path $packageDir "01_catastro\catastro_parcelas.shp"
        default_visible = "si"
        recommendation = "Base principal; usar sin relleno y con borde fino."
    },
    [pscustomobject]@{
        load_order = 6
        layer_code = "catastro_cad_barrios_lineas_ejido"
        target_shape = Join-Path $packageDir "01_catastro\catastro_cad_barrios_lineas_ejido.shp"
        default_visible = "si"
        recommendation = "Apoyo CAD barrial; puede ser pesado."
    },
    [pscustomobject]@{
        load_order = 7
        layer_code = "catastro_cad_barrios_rotulos_ejido"
        target_shape = Join-Path $packageDir "01_catastro\catastro_cad_barrios_rotulos_ejido.shp"
        default_visible = "no"
        recommendation = "Prender solo para lectura de nomenclaturas."
    },
    [pscustomobject]@{
        load_order = 8
        layer_code = "infraestructura_lineas"
        target_shape = Join-Path $packageDir "03_infraestructura\infraestructura_lineas.shp"
        default_visible = "si"
        recommendation = "Infraestructura lineal general."
    },
    [pscustomobject]@{
        load_order = 9
        layer_code = "hidrologia_reservorios_poligonos"
        target_shape = Join-Path $packageDir "04_hidrologia\hidrologia_reservorios_poligonos.shp"
        default_visible = "si"
        recommendation = "Reservorios principales."
    },
    [pscustomobject]@{
        load_order = 10
        layer_code = "hidrologia_reservorios_barreras"
        target_shape = Join-Path $packageDir "04_hidrologia\hidrologia_reservorios_barreras.shp"
        default_visible = "no"
        recommendation = "Apoyo tecnico; prender si se analiza detalle."
    },
    [pscustomobject]@{
        load_order = 11
        layer_code = "hidrologia_reservorios_nombres"
        target_shape = Join-Path $packageDir "04_hidrologia\hidrologia_reservorios_nombres.shp"
        default_visible = "no"
        recommendation = "Etiquetas; prender segun necesidad."
    },
    [pscustomobject]@{
        load_order = 12
        layer_code = "hidrologia_drenaje_cuencas_principales_ejido"
        target_shape = Join-Path $packageDir "04_hidrologia\hidrologia_drenaje_cuencas_principales_ejido.shp"
        default_visible = "no"
        recommendation = "Capa de contexto; cargar antes que lineas de drenaje."
    },
    [pscustomobject]@{
        load_order = 13
        layer_code = "hidrologia_drenaje_cuencas_regiones_ejido"
        target_shape = Join-Path $packageDir "04_hidrologia\hidrologia_drenaje_cuencas_regiones_ejido.shp"
        default_visible = "no"
        recommendation = "Contexto regional de drenaje."
    },
    [pscustomobject]@{
        load_order = 14
        layer_code = "hidrologia_drenaje_lineas_ejido"
        target_shape = Join-Path $packageDir "04_hidrologia\hidrologia_drenaje_lineas_ejido.shp"
        default_visible = "si"
        recommendation = "Red de drenaje dentro del ejido."
    },
    [pscustomobject]@{
        load_order = 15
        layer_code = "hidrologia_drenaje_puentes_ejido"
        target_shape = Join-Path $packageDir "04_hidrologia\hidrologia_drenaje_puentes_ejido.shp"
        default_visible = "si"
        recommendation = "Puntos singulares de drenaje."
    },
    [pscustomobject]@{
        load_order = 16
        layer_code = "referencia_pac_circsect_wgs84"
        target_shape = Join-Path $packageDir "05_referencia_cad\referencia_pac_circsect_wgs84.shp"
        default_visible = "no"
        recommendation = "Referencia historica desde PAC; usar solo para contraste."
    },
    [pscustomobject]@{
        load_order = 17
        layer_code = "referencia_pac_limites_wgs84"
        target_shape = Join-Path $packageDir "05_referencia_cad\referencia_pac_limites_wgs84.shp"
        default_visible = "no"
        recommendation = "Referencia historica desde PAC; usar solo para contraste."
    },
    [pscustomobject]@{
        load_order = 18
        layer_code = "control_drenaje_etiquetas_fuente"
        target_shape = Join-Path $packageDir "99_control\control_drenaje_etiquetas_fuente.shp"
        default_visible = "no"
        recommendation = "Solo control; fuera del ejido final."
    },
    [pscustomobject]@{
        load_order = 19
        layer_code = "catastro_cad_barrios_control_ejido"
        target_shape = Join-Path $packageDir "01_catastro\catastro_cad_barrios_control_ejido.shp"
        default_visible = "no"
        recommendation = "Solo control tecnico del CAD."
    }
)

$catgisLoadOrderPath = Join-Path $controlDir "catgis_load_order_$PackageDate.csv"
$catgisLoadOrder | Export-Csv -Path $catgisLoadOrderPath -NoTypeInformation -Encoding UTF8

$catgisGuidePath = Join-Path $controlDir "CATGIS_CARGA.txt"
@"
CATGIS - orden recomendado de carga

Secuencia sugerida:
1. Cargar primero la base catastral: barrios, zonificacion y parcelas.
2. Cargar despues el CAD barrial de lineas.
3. Dejar rotulos CAD apagados y prenderlos solo si hace falta leer nomenclatura.
4. Cargar infraestructura e hidrologia arriba del catastro.
5. Dejar las capas de referencia PAC y control apagadas.

Consejos practicos:
- parcelas: sin relleno, borde fino.
- zonificacion: relleno con transparencia.
- cad_barrios_lineas_ejido: linea fina y color neutro.
- drenaje_lineas_ejido: color azul.
- drenaje_puentes_ejido: simbolo puntual destacado.

Archivo detallado:
- catgis_load_order_$PackageDate.csv
"@ | Set-Content -Path $catgisGuidePath -Encoding ASCII

$readmePath = Join-Path $packageDir "README.txt"
@"
CATSERVER - entrega shape operativa

Paquete: $packageName
Generado desde: $SourceRoot

Estructura:
- 01_catastro: capas catastrales y CAD barrial final.
- 02_planeamiento: zonificacion.
- 03_infraestructura: lineas de infraestructura.
- 04_hidrologia: reservorios y drenaje final.
- 05_referencia_cad: referencias PAC convertidas desde DWG.
- 99_control: capas o insumos de control, mas manifiestos e inventarios.

Criterio:
- PostGIS es la base maestra.
- Esta carpeta es la salida operativa en shape para usar en CATGIS.
- Las capas con sufijo ejido fueron filtradas espacialmente al ejido urbano.
- drenaje_etiquetas_fuente se deja solo para control porque dentro del ejido no quedaron registros.
- En 99_control se incluye una guia breve y un CSV con orden de carga recomendado para CATGIS.
"@ | Set-Content -Path $readmePath -Encoding ASCII

$zipPath = Join-Path $OutputRoot ($packageName + ".zip")
if (Test-Path -LiteralPath $zipPath) {
    Remove-Item -LiteralPath $zipPath -Force
}

Compress-Archive -Path (Join-Path $packageDir "*") -DestinationPath $zipPath -Force

Write-Host "Paquete generado: $packageDir"
Write-Host "Zip generado: $zipPath"
Write-Host "Manifest: $manifestPath"
