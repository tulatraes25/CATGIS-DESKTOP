# CATGIS / CATMAP Product — Tasks

Status: ACTIVE
Baseline: `main` after PR #217
Requirements: `requirements.md` · Design: `design.md`

Regla: cada tranche se entrega en una rama desde `main` vivo y se **certifica**
(PR head exacto, CI + Certification + Runtime Identity + GeoParquet, merge con
`expected_head_sha`, post-main y atestación). Nada se marca `[x]` sin evidencia.

## Fase P0 — Fundaciones (sin esto todo se construye dos veces)

- [x] **T1 — Contexto y host de render de CATMAP (REQ-PROD-0)**
  - `catmap/app/CatmapAppContext` + `ProjectOpenService` sin GUI; `catmap.Main`
    deja de fabricar estáticos de `CatgisDesktopApp`.
  - `layout/LayoutRenderHost` (proyecto, capas visibles, render de mapa, escala)
    implementado por host CATGIS y host CATMAP.
  - `LayoutMap`/`MapFrameRenderer`/`MapFrameViewport`/`LayoutLabel` dejan de leer
    `AppContext` estático. Ciclo `layout`↔`catmap` resuelto con
    `LayoutElementCodec`.
  - Test: apertura de proyecto y render de página headless sin app principal.
  - Progreso T1a (2026-11-16): **host de render entregado** — `layout/LayoutRenderHost`
    (proyecto + mapPanel) y `layout/LayoutRenderHosts` con adaptador CATGIS por
    defecto (comportamiento legado idéntico), `install/reset` para el host
    standalone y tests; migradas **todas** las lecturas estáticas de `layout/`:
    `LayoutLabel`, `LayoutMap`, `MapFrameRenderer`, `MapFrameViewport`,
    `LayoutTemplateManager` y `LayoutContext`. Tests `LayoutRenderHostTest` (5);
    build local **1400 tests / 0 fallos / 15 skips**. Falta T1b
    (`CatmapAppContext`/`ProjectOpenService` y quitar la fábrica de estáticos de
    `catmap.Main`) y T1c (codec del ciclo `layout`↔`catmap`).
  - Progreso T1b (2026-11-16): **contexto standalone y apertura headless
    entregados** — `catmap/app/CatmapAppContext` centraliza el bootstrap
    (proyecto + MapPanel/LayersPanel/StatusBar, `standalone()`/`install()`) y
    `catmap.Main` ya no fabrica los tres estáticos de `CatgisDesktopApp` ni fija
    el proyecto a mano; `ProjectOpenService.inspect(File)` valida y parsea un
    `.catgis` **sin efectos Swing** (cabecera, CRS, metadatos, grupos, vista,
    capas vía `LoadProjectAction.parseLayer`, normalización CATSERVER y preflight
    de fuentes locales) y devuelve `ProjectInspection`; CATMAP pre-valida con él
    antes de aplicar el proyecto (menú Abrir y `--project`). Tests
    `ProjectOpenServiceTest` (5) y `CatmapAppContextTest` (2).
  - Progreso T1c (2026-11-16): **ciclo `layout`↔`catmap` roto** — `layout`
    declara `LayoutElementCodec`/`LayoutDocumentCodec` y el registro
    `LayoutCodecs` (instalación explícita + puente reflectivo perezoso a
    `CatmapSerializer`/`CatmapDocumentIO`); `LayoutModel` (undo/redo) y
    `LayoutTemplateManager` (guardar/cargar plantilla) ya no importan ni
    referencian `ar.com.catgis.catmap` (sólo cadenas `Class.forName` dentro del
    puente). Guard de arquitectura `LayoutPackageDependencyGuardTest` (3) falla
    si vuelve una referencia directa; `LayoutCodecContractTest` (5) cubre
    roundtrip elemento/documento sin instalación explícita, undo por el puente y
    override instalado. Build local **1476 tests / 0 fallos / 15 skips**.
- [x] **T2 — Página como estado del documento (REQ-PROD-2)**
  - `LayoutPageSpec` (preset/CUSTOM mm, orientación, márgenes, DPI) en el modelo
    y en el serializador; migración de `.catmap` v1/v2.
  - Test: roundtrip A3 vertical + custom mm; export dimensiones exactas.
  - Evidence (2026-11-15): `layout/LayoutPageSpec.java` + `LayoutModel.pageSpec`;
    `CatmapSerializer` persiste `PAGE_SIZE`/`PAGE_ORIENTATION` y agrega
    `PAGE_WIDTH_MM`/`PAGE_HEIGHT_MM`/`PAGE_MARGINS_MM` cuando la página no es A4
    horizontal (legado carga A4 horizontal; corrupto falla cerrado);
    `LayoutExportEngine` (PNG/JPG/PDF), `SvgExportEngine`, `AtlasEngine` y el
    preview/alineación de `catmap.Main` usan la página del documento. Tests
    nuevos: `LayoutPageSpecTest` (7) + `CatmapPageGeometryContractTest` (10);
    build local **1378 tests / 0 fallos / 15 skips**.
- [x] **T3 — Documento único y IO (REQ-PROD-0/2)**
  - `CatmapDocumentIO` reemplaza los dos formatos; migra JSON del compositor y
    líneas del standalone; falla explícito ante formato desconocido.
  - Tests de compatibilidad de ambos legados y de error.
  - Evidence (2026-11-16): `catmap/CatmapDocumentIO` (detección por contenido,
    save canónico atómico, lectura del JSON legado vía Jackson) cableado en
    `catmap.Main`; `LayoutTemplateManager.saveTemplate/loadTemplate` delegan en
    el IO (el compositor ya no escribe JSON). Migración sin pérdida ampliada en el
    formato canónico: grilla del mapa (`GRID_*`, `TARGET_SCALE`) y leyenda
    (`SHOW_BORDER`, tamaños de fuente); `LayoutPageSpec.fromPortraitDimensions`
    normaliza el legado a preset A4 cuando corresponde. Fallos cerrados:
    formato desconocido, `format` ajeno, tipo de elemento no soportado o
    coordenada faltante. Tests `CatmapDocumentIOTest` (10); build local
    **1388 tests / 0 fallos / 15 skips**.
- [x] **T4 — Servicio único de export (REQ-PROD-3)**
  - `LayoutExportService.renderPage(model, pageIndex, pageSpec, metrics)` para
    PDF multipágina, PNG/JPG/ZIP, SVG, print y atlas; verificador parametrizado.
  - Test: PDF de 3 páginas con tamaño exacto.
  - Evidence (2026-11-16): `catmap/LayoutExportService` con
    `renderPage(model,pageIndex,dpi)` (página real del documento),
    `exportPdf` multipágina (todas las páginas del documento en un PDF) y
    `exportPngPages` (un PNG atómico por página, nombre validado, staging y
    `verifyImage`); `LayoutExportEngine.renderLayout` delega en el servicio y
    `writePdf` itera las páginas; `CatmapAtomicExport.verifyPdf` parametrizado
    por cantidad de páginas y tamaño de hoja; `LayoutModel` expone
    `getVisibleElementsSortedByZ(page)`. Inventario/mapeo semántico actualizados
    (regla del repo). Tests `LayoutExportServiceContractTest` (8); build local
    **1395 tests / 0 fallos / 15 skips**.

## Fase P1 — Cartografía correcta

- [x] **T5 — Métricas DPI únicas (REQ-PROD-3.3)**
  - `RenderMetrics` (pt/mm→px `dpi/72`) en labels, escala, leyenda, tabla,
    cartouche y etiquetas del mapa; quitar 96 fijo en `LayoutMap`.
  - Test "preview == export" 96/150/200 DPI con tolerancia declarada.
  - Evidence (2026-11-16): `layout/RenderMetrics` convierte tamanos de layout
    (px base 96 DPI, la metrica historica de las plantillas) a pixeles de
    dispositivo con `size*dpi/96` y expone `scaledFont` y el round-trip px→base;
    aplicado a `LayoutLabel` (fuente, halo, subrayado) y `LayoutScaleBar`
    (etiquetas y ratio, con el clamp historico escalado por DPI). Los
    exportadores vectoriales PDF/SVG convierten px@96 → pt (`×0.75`) para
    compartir el tamano fisico del raster. Contrato
    `LayoutTextDpiParityContractTest`: la tinta del texto escala 2× entre 96 y
    192 DPI (paridad fisica preview/export); `RenderMetricsContractTest` (4).
    Los goldens existentes no cambian a 96 DPI y la politica de tolerancia por OS
    queda intacta.
- [x] **T6 — Escala derivada (REQ-PROD-5)**
  - `ScaleResolver` compartido; barra recalculada en pan/zoom y export.
  - Evidence (2026-11-16): `layout/ScaleResolver` deriva el denominador del
    viewport y tamaño de píxel del marco (proyectado por unidad del CRS;
    geográfico con corrección de latitud `111320·cos(lat)`), con
    `withResolvedScale` que renderiza una copia de la barra (el documento nunca
    se muta) y `autoScale` en `LayoutScaleBar` (default true). Cableado en
    `LayoutExportService.renderPage` (PNG/PDF raster/goldens),
    `LayoutExportEngine` (PDF raster histórico), `AtlasEngine` y la vista previa
    standalone. Contrato `ScaleResolverContractTest` (5) y paridad de render con
    zoom 1×/4× en `LayoutTextDpiParityContractTest`.
- [x] **T7 — Configurar página en UI (REQ-PROD-2)**
  - Diálogo A3/A4/A5/Carta/Legal/Tabloide/custom + orientación/márgenes/DPI;
    preview, export, atlas y print usan la `PageSpec`.
  - Evidence (2026-11-16): `catmap/ui/PageSetupDialog` (presets + Personalizado
    en mm + orientación + márgenes, validación en línea; `buildSpec` puro y
    testeado headless) y ítem "Páginas → Configurar página…" en `catmap/Main`
    que aplica la spec al modelo y reconstruye la vista previa; guardar/cargar,
    export PDF/PNG/SVG y atlas usan la página del documento (T2/T3/T4).
    Inventarios actualizados en el mismo PR (surface, mapeo semántico,
    suplementario visible y sus baselines). Tests
    `PageSetupDialogContractTest` (7); build local **1407 tests / 0 fallos /
    15 skips**.
- [x] **T8 — Impresión profesional (REQ-PROD-3.5)**
  - `LayoutPrintable` con `PageFormat` del documento y `Book` multipágina; sin
    raster fijo 200 DPI.
  - Evidence (2026-11-16): `catmap/print/LayoutPrintable` (render por página con
    el mismo camino de export, escalado dentro del área imprimible, NO_SUCH_PAGE
    fuera de rango) y `catmap/print/LayoutPrintService` (`buildBook` con un
    `PageFormat` por página: hoja del documento en mm, orientación y márgenes
    como área imprimible; `print()` con diálogo opcional). `catmap.Main` ya no
    rasteriza A4 fijo: "Imprimir…" imprime todas las páginas del documento.
    Inventario actualizado (`SERVICE catmap/print/LayoutPrintService.java`).
    Tests `LayoutPrintServiceContractTest` (7); build local **1414 tests / 0
    fallos / 15 skips**.

## Fase P2 — Formatos y atlas

- [x] **T9 — PDF multipágina con fuentes (REQ-PROD-3.1)**
  - Evidence (2026-11-16): `catmap/PdfVectorExporter` escribe PDF vectorial
    multipágina con la hoja del documento (mm→pt, A3/A4/custom), texto real
    extraíble (Helvetica/Helvetica-Bold estándar, texto saneado a WinAnsi) y
    formas vectoriales: labels (con underline), rectángulos, elipses (bézier),
    líneas (con dash), barra de escala (ticks + textos), leyenda (fondo, título,
    símbolos), cartouche (campos), tabla (filas) y flecha norte (polígonos);
    sólo el marco de mapa se rasteriza (PNG 200 DPI embebido, presupuesto de
    píxeles). Publicación atómica + validación independiente con PDFBox
    (cantidad de páginas y MediaBox); "Exportar PDF" usa el exportador
    vectorial; el PDF de atlas y `LayoutExportEngine` quedan como ruta raster
    histórica. Inventarios actualizados (`EXPORT_SURFACE` +
    `SEM-SURFACE-PDFVECTOREXPORTER`). Tests `PdfVectorExporterContractTest` (5);
    build local **1450 tests / 0 fallos / 15 skips**.
- [x] **T10 — SVG vectorial real (REQ-PROD-7.2)**
  - Evidence (2026-11-16): `catmap/SvgVectorExporter` emite SVG nativo con hoja
    del documento (mm + viewBox), fondo, y elementos vectoriales reales:
    `<text>` (labels con familia/tamaño/peso/color/underline, tokens dinámicos
    ya resueltos por el modelo), `<rect>`, `<ellipse>`, `<line>`, barra de escala
    (líneas + ticks + textos), leyenda (fondo, título, símbolos y etiquetas),
    cartouche (campos clave/valor), tabla (filas y separadores) y flecha norte
    (polígonos + N). Sólo el marco de mapa y elementos sin geometría vectorial
    (imagen, graticule) se incrustan como PNG data URI a 150 DPI, con presupuesto
    de píxeles. Publicación atómica + validación XML independiente (raíz/hoja,
    sin `<script>`/`foreignObject`, imágenes sólo `data:image/png;base64`, al
    menos un elemento dibujable); "Exportar SVG" usa el exportador vectorial.
    Inventarios actualizados (`EXPORT_SURFACE` + `SEM-SURFACE-SVGVECTOREXPORTER`).
    Tests `SvgVectorExporterContractTest` (6); build local **1445 tests / 0
    fallos / 15 skips**.
- [x] **T11 — GeoTIFF georreferenciado (REQ-PROD-7.1)**
  - Export del marco con CRS/extent; test ida/vuelta `GeoTiffReader`.
  - Evidence (2026-11-16): `catmap/GeoTiffLayoutExporter` exporta el marco de mapa
    visible de la página activa renderizado con su viewport
    (`LayoutMap.viewportForFrame`, compartido con el render normal) a GeoTIFF con
    el CRS del proyecto y el envelope del viewport, publicación atómica y
    validación independiente con `GeoTiffReader` (CRS, extent y tamaño de ráster)
    antes de reemplazar el archivo; "Exportar imagen…" acepta `.tif/.tiff` además
    de PNG/JPG (sin sumar comandos). Inventarios surface/semántico/matriz
    actualizados (`SEM-SURFACE-GEOTIFFLAYOUTEXPORTER`). Tests
    `GeoTiffLayoutExporterContractTest` (5: CRS/extent/tamaño, reemplazo con
    cabecera TIFF, fallo cerrado sin marco y sin CRS, marco inválido); build local
    **1419 tests / 0 fallos / 15 skips**.
- [x] **T12 — Atlas v2 (REQ-PROD-4)**
  - Encuadre centro+buffer+aspecto, orden/nombre, `{page}`/`{feature}`,
    configuración persistida, PDF/PNG/SVG.
  - Evidence (2026-11-16): `catmap/AtlasFraming` (centro del feature + buffer %
    + ajuste al aspecto del marco sin deformar; punto degenerado con span
    mínimo) aplicado por `AtlasEngine` a cada copia del marco (la plantilla no se
    muta); tokens `{page}`/`{feature}` sustituidos en texto y expresión dinámica
    de cada página; `AtlasEngine.exportPdf` multipágina con hoja del documento,
    publicación atómica y `verifyPdf` parametrizado; orden asc/desc de features
    y buffer configurables; configuración del atlas persistida en `.catmap`
    (`ATLAS_COVERAGE_LAYER`, `ATLAS_NAME_FIELD`, `ATLAS_BUFFER_PERCENT`,
    `ATLAS_ORDER`) con defaults seguros y fallo cerrado ante valores corruptos;
    UI de atlas con PNG/PDF, margen y orden. Tests `AtlasFramingContractTest`
    (6), `AtlasV2ContractTest` (8) y safety test actualizado; build local
    **1433 tests / 0 fallos / 15 skips**.
- [x] **T13 — Plantillas en standalone (REQ-PROD-6)**
  - Galería por categorías + QPT con página/orientación.
  - Evidence (2026-11-16): `catmap/TemplateApplicationService` (catálogo completo
    agrupado por categoría; aplicar plantilla reemplaza el layout y adopta la
    hoja inferida —A4 horizontal para el catálogo actual, A3 como fallback—;
    importar `.qpt` reemplaza elementos y adopta página/orientación del QPT, y
    **sólo modifica el modelo si el QPT aporta elementos**; fallo cerrado para
    plantilla desconocida, QPT corrupto o QPT vacío) y
    `catmap/ui/TemplateGalleryDialog` (categorías + lista + Aplicar +
    Importar QPT). Menú "Páginas → Plantillas…" en `catmap.Main`; inventarios
    surface/semántico/suplementario actualizados en el mismo PR. Tests
    `TemplateApplicationServiceContractTest` (7); build local **1440 tests / 0
    fallos / 15 skips**.

## Fase P3 — Datos del usuario (local-first)

- [x] **T14 — Mapa rápido en CATMAP (REQ-PROD-1.1)**
  - Abrir SHP/GeoJSON/KML/KMZ/GeoPackage/GeoTIFF/CSV sin proyecto previo, CRS
    explícito y fallo cerrado.
  - Evidence (2026-11-16): `catmap/QuickMapService` acepta SHP/GeoJSON/KML/KMZ/
    GeoPackage/SpatiaLite/FGB/GPX/DXF/DWG/Parquet/GeoParquet, GeoTIFF y rasters
    (TIFF/PNG/JPG/BMP/GIF/IMG), NetCDF/GRIB y CSV; los formatos vectoriales y
    raster reutilizan el flujo endurecido `OpenFileAction` (CRS del formato,
    fallo cerrado); el CSV se convierte en capa de puntos con
    `CsvTableReader` + `TablePointLayerBuilder`, detectando columnas X/Y
    (x/lon/longitud/este y y/lat/latitud/norte) y **exigiendo CRS explícito**
    salvo columnas lat/lon (EPSG:4326), creando el proyecto mínimo "Mapa rápido"
    e instalando capa en AppContext/MapPanel. UI "Archivo → Abrir datos…" con
    selector que pide CRS para CSV, y arranque `--open <archivo>`. Inventarios
    actualizados (`SERVICE QuickMapService`, comando suplementario
    `SEM-CATMAP-QUICKMAP`). Tests `QuickMapServiceContractTest` (6); build local
    **1482 tests / 0 fallos / 15 skips**.
- [ ] **T15 — Servicios OGC y PostGIS opcional (REQ-PROD-1.2)**
  - WFS/WMS/WMTS y PostGIS conectables desde standalone; PostGIS sigue opcional.
- [ ] **T16 — Workspace de datos (REQ-PROD-10 parcial)**
  - Panel proyecto/datos, capas con simbología, jobs/resultados; sin modales por
    archivo.

## Fase P4 — Terreno, hidrología y riesgo

- [x] **T17 — Rasters analíticos exportables (REQ-PROD-8.1)**
  - Pipeline `AnalysisRaster` float/NoData/unidad + GeoTIFF + estadísticas para
    pendiente, aspecto, hillshade, dirección y acumulación; sin truncado 8 bits.
  - Fixtures numéricos con pendiente conocida.
  - Evidence (2026-11-16): `catmap/AnalysisRaster` (valores `float[]`, NoData,
    unidad, extent+CRS explícito, `stats()` min/max/media/desvío sobre celdas
    válidas, `exportGeoTiff` con publicación atómica + reapertura GeoTIFF que
    valida CRS/extent/dimensiones, `exportAsciiGrid` ESRI con cabecera y
    verificación de cantidad de valores) y `catmap/TerrainRasterAlgorithms`
    (pendiente y aspecto por método de Horn, hillshade azimut/elevación
    configurables, dirección D8 y acumulación D8 por orden de elevación, con
    propagación de NoData; **sin cuantización 8 bits**). Fixtures numéricos:
    plano ascendente al este con pendiente `atan(0.1)=5.7106°`, aspecto 270°,
    D8=16 y acumulación 9; plano horizontal con hillshade `cos(45°)=0.7071`.
    Tests `AnalysisRasterContractTest` (4) y
    `TerrainRasterAlgorithmsContractTest` (6); build local **1492 tests / 0
    fallos / 15 skips**. Las salidas de los diálogos existentes quedan como
    imágenes de preview hasta T20/T21.
- [x] **T18 — Resolución nativa y umbrales en área (REQ-PROD-8.2)**
  - Eliminar downsample fijo; umbrales en m²/ha además de celdas; performance
    acotada y cancelable (fuera del EDT).
  - Evidence (2026-11-16): `AnalysisDetail.NATIVE` (valor por defecto en servicio
    y diálogos) muestrea el DEM **sin remuestreo** con presupuesto explícito
    `MAX_NATIVE_CELLS = 4.000.000` celdas y fallo cerrado con mensaje si el DEM
    lo excede; los modos Rápida/Equilibrada/Detallada quedan como reducciones
    explícitas. Umbrales en área: sobrecarga
    `generateDrainage(..., double thresholdAreaM2, ...)` y
    `resolveThresholdCells` (convierte m² a celdas con el área de celda real,
    exige ≥2 celdas, degrada a celdas si el área es desconocida), y la capa de
    salida ahora declara `threshold_cells`, `threshold_area_m2` y
    `threshold_ha`. Cancelación: `generateDrainageCancellable(..., BooleanSupplier)`
    con chequeos en el muestreo (cada 32 filas), acondicionamiento, dirección y
    acumulación que lanzan `CancellationException`; el diálogo de escorrentías
    ejecuta en `SwingWorker` (fuera del EDT) y el botón Cancelar ahora cancela
    el trabajo en curso. Tests `DrainageNativeResolutionContractTest` (6:
    conversión de umbral, presupuesto, resolución nativa igual al DEM,
    umbral por área aplicado, cancelación y etiqueta del modo); build local
    **1498 tests / 0 fallos / 15 skips**.
- [x] **T19 — Morfometría de cuencas (REQ-PROD-8.3)**
  - Área, perímetro, elevación/pendiente media, Strahler, densidad de drenaje;
    zonal stats reutilizado.
  - Evidence (2026-11-16): `catmap/BasinMorphometry` delimita la cuenca aguas
    arriba del outlet con D8 inverso (BFS sobre la grilla de direcciones,
    respetando NoData) y calcula un `BasinStats` con celdas, **área km²**,
    **perímetro km** (longitud de bordes de celda no compartidos), elevación
    mínima/máxima/media, **relieve**, **pendiente media** (usa el raster de
    pendiente o lo deriva con `TerrainRasterAlgorithms`), **orden de Strahler**
    máximo de la red (canales = acumulación ≥ umbral; orden calculado
    aguas-arriba en orden de elevación descendente) y **densidad de drenaje**
    (longitud de canales por área). Utilidades `basinCellCount` y
    `channelCells` para reportes. Tests `BasinMorphometryContractTest` (5):
    plano con geometría conocida (área 0.002 km², perímetro 0.42 km, media
    14.25 m, densidad 90 km/km²), cuenca de una celda en plano, valle con dos
    flancos (Strahler 2), validaciones de geometría/outlet y NoData; build local
    **1503 tests / 0 fallos / 15 skips**. La integración visual en el diálogo
    existente queda para T20/T21.
- [x] **T20 — DEM acondicionado exportable (REQ-PROD-8.4)**
  - Evidence (2026-11-16): `DrainageExtractionService.buildConditionedDem` (y
    variante cancelable) expone el DEM corregido (sinks/flats) como
    `ConditionedDemExport` con raster `AnalysisRaster` float en metros
    (`NO_DATA` en celdas inválidas) y **trazabilidad** de `detail`,
    `conditioning` y CRS de origen; geometría y resolución nativa del DEM
    (sin remuestreo). Exportable a GeoTIFF/ASCII con la validación de T17.
    Tests `ConditionedDemExportContractTest` (4): relleno del pozo conservando
    la pendiente y geometría nativa, NoData preservado, GeoTIFF reabierto con
    CRS/dimensiones, cancelación y rechazo de capas no raster; build local
    **1507 tests / 0 fallos / 15 skips**.
- [x] **T21 — Viewshed en producto (REQ-PROD-8.5)**
  - UI con observador/radio; capa de resultado; test del motor existente.
  - Evidence (2026-11-16): `catmap/ViewshedRaster` envuelve el motor existente
    (`TerrainHydrologyAnalysisService.computeViewshed`) sobre `AnalysisRaster`:
    máscara float (visible=1, oculta=0, fuera del radio/NoData=NO_DATA), overlay
    ARGB (verde visible, transparente oculto, marcador naranja del observador) y
    validaciones de observador/altura/radio. `ViewshedDialog` (nuevo) permite
    elegir DEM, observador X/Y (precargado al centro del DEM), altura y radio en
    metros, calcula en `SwingWorker` (cancelable) y **agrega la capa de
    resultado** al proyecto/mapa como raster derivado `viewshed` con parámetros
    registrados; el menú "Visibilidad / Viewshed..." ahora abre este diálogo en
    lugar del análisis genérico. Tests `ViewshedRasterContractTest` (4) además
    del test del motor existente; build local **1517 tests / 0 fallos / 15
    skips**. Inventario surface/semántico/matriz actualizado con el diálogo.
- [x] **T22 — Perfil completo (REQ-PROD-8.6)**
  - Pendiente del perfil, curva hipsométrica, perfil 3D.
  - Evidence (2026-11-16): `catmap/TerrainProfile` muestrea el perfil con
    interpolación bilineal (NoData → NaN), calcula **pendiente por muestra**
    (diferencias centrales), pendiente media/máxima, ascenso y descenso
    acumulados, longitud, min/máx/media y relieve, **curva hipsométrica**
    (altura relativa vs área relativa en N bandas) y **línea 3D** JTS con la
    elevación en Z. Validaciones de DEM/extensión/longitud/muestras/bandas.
    Tests `TerrainProfileContractTest` (5: plano conocido exacto, curva
    monótona, línea 3D con Z, NoData sin invalidar métricas, validaciones);
    build local **1527 tests / 0 fallos / 15 skips**. Integración visual del
    perfil en el diálogo existente queda para P5/T26+.
- [x] **T23 — Volumen/corte-relleno (REQ-PROD-8.7)**
  - Evidence (2026-11-16): `catmap/VolumeCalculator` calcula corte/relleno sobre
    `AnalysisRaster` float: `againstPlane` (DEM contra cota de referencia) y
    `between` (DEM existente vs propuesto) con `VolumeResult` (celdas válidas,
    área m², corte m³, relleno m³, neto, mínimo/máximo/media del delta); las
    celdas NoData se excluyen y los rasters deben compartir grilla, extensión y
    CRS (tolerancia relativa). `differenceRaster` produce el raster de diferencia
    de elevación exportable a GeoTIFF/ASCII con NoData preservado. Tests
    `VolumeCalculatorContractTest` (5: corte exacto contra plano, neto entre DEMs
    y signo invertido, exclusión de NoData, raster de diferencia con NoData y
    validaciones de geometría/entradas); build local **1522 tests / 0 fallos /
    15 skips**. La integración visual (diálogo de volumen) queda para P5/T26+.
- [x] **T24 — Interpolación desde puntos (REQ-PROD-8.8)**
  - IDW propio; evaluar kriging con dependencia verificable.
  - Evidence (2026-11-16): `catmap/IdwInterpolator` implementa IDW propio
    (muestras finitas, potencia, vecinos máximos y radio de búsqueda
    configurables, sin dependencia externa) sobre la grilla `AnalysisRaster`
    float con NoData, límite de `4.000.000` celdas y CRS explícito; las celdas
    sin muestras en el radio quedan NoData y un nodo exacto conserva su valor.
    `describe(...)` reporta parámetros para trazabilidad. Kriging: se evaluó y
    **no se incorpora** porque requeriría una dependencia externa con licencia y
    estabilidad verificables; IDW cubre el requisito y la decisión queda
    registrada. Tests `IdwInterpolatorContractTest` (6: plano conocido con error
    acotado, nodos exactos, huecos por radio, validaciones, GeoTIFF de ida y
    vuelta, descripción); build local **1513 tests / 0 fallos / 15 skips**.
- [x] **T25 — Riesgo e inundación calibrados (REQ-PROD-9)**
  - Quitar 30 m/111320; clases de susceptibilidad, exposición, parámetros
    persistidos; `WILDFIRE` implementado o retirado; periodo de retorno
    documentado; trazabilidad de algoritmo/parámetros/CRS.
  - Evidence (2026-11-16): `climate/GeographicMetrics` reemplaza el 111320 fijo
    con fórmulas WGS84 (radios de curvatura y metros/grado por latitud);
    `RiskAnalysisEngine` recibe `RiskParameters` explícitos y persistibles
    (`cellSizeMeters` con fallback documentado `DEFAULT_CELL_SIZE_METERS`,
    `returnPeriodYears` con `DEFAULT_RETURN_PERIOD_YEARS` documentado y
    `exposureFactor` aplicado antes de normalizar), expone 5 **clases de
    susceptibilidad** (Muy baja…Muy alta) con `classifySusceptibility`, y cada
    `RiskResult` incluye `parameterSpec` con **trazabilidad** (algoritmo
    `risk-analysis/1`, CRS `EPSG:4326`, escenario, celda, periodo de retorno,
    exposición, clases y `wildfire=withdrawn`). El escenario **WILDFIRE fue
    retirado** del enum por no tener modelo calibrado (documentado). Tests
    `RiskAnalysisEngineCalibrationTest` (5) + suite previa del motor; build
    local **1532 tests / 0 fallos / 15 skips**.

## Fase P5 — UX "Filmora del GIS"

- [ ] **T26 — Salida segura y notificaciones (REQ-PROD-10.1/10.2)**
  - Confirmación universal de cambios (incluye `catmap.Main`); toasts para carga
    masiva en lugar de N modales.
- [x] **T27 — Paleta de comandos (REQ-PROD-10.3)**
  - Búsqueda por tarea sobre comandos registrados; integración con inventarios.
  - Evidence (2026-11-16): `catmap/ui/CommandPaletteIndex` (modelo puro:
    comandos con id/etiqueta/descripción/keywords/acción; búsqueda por tokens con
    normalización de acentos y ranking etiqueta > keywords > descripción) y
    `catmap/ui/CommandPaletteDialog` (búsqueda incremental, navegación con
    flechas/Enter/ESC y ejecución de la acción). `catmap.Main` registra **18
    comandos reales** (layout, proyecto, datos, guardar, páginas, plantillas,
    atlas, exportaciones, impresión) y suma "Archivo → Buscar comando..." a los
    inventarios (surface + semántico + suplementario) con baselines actualizados.
    Tests `CommandPaletteIndexContractTest` (5: ranking, acentos/mayúsculas,
    tareas por keywords, sin resultados y validaciones); build local **1538
    tests / 0 fallos / 15 skips**.
- [ ] **T28 — HiDPI y temas (REQ-PROD-10.4)**
- [ ] **T29 — i18n honesta (REQ-PROD-10.5)**
  - Sin traducción automática parcial; completar inglés o retirar del selector;
    títulos hardcodeados eliminados.
- [ ] **T30 — Limpieza de UI muerta y duplicados (REQ-PROD-10.6)**
  - Toolbars jamás agregados, paneles vacíos, exporters duplicados, welcome page
    inalcanzable.

## Fase P6 — Verificación y release

- [ ] **T31 — Golden masters y caracterización A0.2 (REQ-PROD-11)**
  - Escenas deterministas con hash, roundtrips, preview=export; antes de tocar
    `MapPanel`/compositor.
- [ ] **T32 — Verificación runtime cuando A0.7 lo permita (REQ-PROD-12.3)**
  - Snapshot de UI en Actions/Xvfb y/o Codespaces; sin claims hasta que A0.7/F3
    estén certificados.
- [ ] **T33 — Empaquetado y launcher CATMAP independiente (REQ-PROD-0)**
  - App image propio, iconos, splash, actualización de `build.gradle` y docs.

## Dependencias

- T2→T3→T4→T5→T6→T7→T8 (fundaciones a cartografía).
- T12 depende de T4 y T2; T13 de T2.
- T17 depende de T5 (métricas) y del pipeline raster; T19 de T17.
- T26–T30 pueden avanzar en paralelo con P1–P2 usando ramas disjuntas.
- T31 es obligatorio antes de cualquier extracción de `MapPanel`/compositor.

## Evidencia inicial recogida (2026-11-15)

- Reconocimiento CATMAP standalone: 13 brechas con archivo:línea (`design.md` §1.1).
- Reconocimiento terreno/hidrología: 11 brechas con archivo:línea (`design.md` §1.2).
- Reconocimiento datos/UX: formatos soportados y 10 problemas UX prioritarios.
- Higiene previa: T5–T8 del backlog cerrados (ramas 160→47, advisories 0, docs
  históricos anotados, inventario de skips).
