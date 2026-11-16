# CATGIS / CATMAP Product — Tasks

Status: ACTIVE
Baseline: `main` after PR #217
Requirements: `requirements.md` · Design: `design.md`

Regla: cada tranche se entrega en una rama desde `main` vivo y se **certifica**
(PR head exacto, CI + Certification + Runtime Identity + GeoParquet, merge con
`expected_head_sha`, post-main y atestación). Nada se marca `[x]` sin evidencia.

## Fase P0 — Fundaciones (sin esto todo se construye dos veces)

- [ ] **T1 — Contexto y host de render de CATMAP (REQ-PROD-0)**
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
  - Evidence (2026-11-16): `layout/RenderMetrics` (points→pixels `size*dpi/72`,
    `scaledFont`, round-trip px→pt) aplicado a `LayoutLabel` (fuente, halo y
    subrayado) y `LayoutScaleBar` (etiquetas y ratio con clamp dependiente de
    DPI); leyenda/tabla/cartouche/graticule ya escalaban con `dpi/72` y quedan
    consistentes. Contrato `LayoutTextDpiParityContractTest`: el ancho/alto de
    tinta del texto escala 2× entre 96 y 192 DPI (paridad física preview/export),
    y `RenderMetricsContractTest` (4). Tras la revisión visual se regeneraron de
    forma intencional las baselines `label`, `scalebar`, `pipeline`,
    `pipeline-map` y `catmap-real-data` (el texto pasó a su tamaño físico
    correcto); la política de tolerancia por OS y el delta no cambiaron.
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

- [ ] **T14 — Mapa rápido en CATMAP (REQ-PROD-1.1)**
  - Abrir SHP/GeoJSON/KML/KMZ/GeoPackage/GeoTIFF/CSV sin proyecto previo, CRS
    explícito y fallo cerrado.
- [ ] **T15 — Servicios OGC y PostGIS opcional (REQ-PROD-1.2)**
  - WFS/WMS/WMTS y PostGIS conectables desde standalone; PostGIS sigue opcional.
- [ ] **T16 — Workspace de datos (REQ-PROD-10 parcial)**
  - Panel proyecto/datos, capas con simbología, jobs/resultados; sin modales por
    archivo.

## Fase P4 — Terreno, hidrología y riesgo

- [ ] **T17 — Rasters analíticos exportables (REQ-PROD-8.1)**
  - Pipeline `AnalysisRaster` float/NoData/unidad + GeoTIFF + estadísticas para
    pendiente, aspecto, hillshade, dirección y acumulación; sin truncado 8 bits.
  - Fixtures numéricos con pendiente conocida.
- [ ] **T18 — Resolución nativa y umbrales en área (REQ-PROD-8.2)**
  - Eliminar downsample fijo; umbrales en m²/ha además de celdas; performance
    acotada y cancelable (fuera del EDT).
- [ ] **T19 — Morfometría de cuencas (REQ-PROD-8.3)**
  - Área, perímetro, elevación/pendiente media, Strahler, densidad de drenaje;
    zonal stats reutilizado.
- [ ] **T20 — DEM acondicionado exportable (REQ-PROD-8.4)**
- [ ] **T21 — Viewshed en producto (REQ-PROD-8.5)**
  - UI con observador/radio; capa de resultado; test del motor existente.
- [ ] **T22 — Perfil completo (REQ-PROD-8.6)**
  - Pendiente del perfil, curva hipsométrica, perfil 3D.
- [ ] **T23 — Volumen/corte-relleno (REQ-PROD-8.7)**
- [ ] **T24 — Interpolación desde puntos (REQ-PROD-8.8)**
  - IDW propio; evaluar kriging con dependencia verificable.
- [ ] **T25 — Riesgo e inundación calibrados (REQ-PROD-9)**
  - Quitar 30 m/111320; clases de susceptibilidad, exposición, parámetros
    persistidos; `WILDFIRE` implementado o retirado; periodo de retorno
    documentado; trazabilidad de algoritmo/parámetros/CRS.

## Fase P5 — UX "Filmora del GIS"

- [ ] **T26 — Salida segura y notificaciones (REQ-PROD-10.1/10.2)**
  - Confirmación universal de cambios (incluye `catmap.Main`); toasts para carga
    masiva en lugar de N modales.
- [ ] **T27 — Paleta de comandos (REQ-PROD-10.3)**
  - Búsqueda por tarea sobre comandos registrados; integración con inventarios.
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
