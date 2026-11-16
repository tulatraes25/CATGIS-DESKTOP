# CATGIS / CATMAP Product — Design

Status: ACTIVE
Baseline: `main` after PR #217
Requirements: `requirements.md`

## 1. Estado actual (evidencia del reconocimiento 2026-11-15)

### 1.1 CATMAP standalone (`catgis-desktop/src/ar/com/catgis/catmap/`)

Existe una app independiente real (`catmap/Main.java:37-102`, main-class jpackage
`build.gradle:410-415`), con modelo multipágina, serialización atómica y backup,
atlas PNG acotado, y export PNG/JPG/PDF/SVG/print. Brechas verificadas:

| Brecha | Evidencia |
|---|---|
| Página fija A4 horizontal en preview/export/atlas/alineación | `catmap/Main.java:120-122`, `:1110-1111`, `LayoutExportEngine.java:457-460`, `SvgExportEngine.java:32-33`, `AtlasEngine.java:52-55` |
| `LayoutModel` sin tamaño/orientación; serializador escribe constantes e ignora al cargar | `LayoutModel.java:9-31`, `CatmapSerializer.java:111-117`, `:213-229` |
| PDF de una sola página; verificador exige 1 página | `LayoutExportEngine.java:149-159`, `CatmapAtomicExport.java:170-172` |
| Atlas PNG con encuadre incorrecto (usa `zoomFactor` como extent) | `catmap/Main.java:531-542`, `AtlasEngine.java:93-101` |
| Escala manual (default 1:10.000), nunca derivada del mapa | `LayoutScaleBar.java:43-46`, `:102-107`; `catmap/Main.java:810-814` |
| Tres convenciones de DPI (labels px fijos, leyenda dpi/72, mapa 96 fijo) | `LayoutLabel.java:136-154`, `LayoutLegend.java:167-171`, `LayoutMap.java:341-360` |
| Dos formatos `.catmap` incompatibles y con pérdida de tipos | `CatmapSerializer.java` vs `LayoutTemplateManager.java:312-345`, `:377-382` |
| Plantillas/QPT solo en el compositor embebido | `MapLayoutComposerDialog.java:729-905`, `:4340-4354` |
| Sin carga de datos sueltos (solo abre `.catgis`) | `catmap/Main.java:148`, `:1170-1184` |
| SVG raster (PNG embebido); sin GeoTIFF | `SvgExportEngine.java:22-82` |
| Impresión fija 200 DPI, 1 página, sin `pageDialog` | `catmap/Main.java:1324-1359` |
| "Salir" sin confirmar cambios | `catmap/Main.java:162` |
| Acoplamiento: fabrica estáticos de `CatgisDesktopApp`; ciclo `layout`↔`catmap` | `catmap/Main.java:62-66`; `LayoutModel.java:66,77`; `CatmapSerializer.java:5` |

### 1.2 Terreno / hidrología (`catgis-desktop/src/ar/com/catgis/`)

Fortaleza: ya existen motores Java para curvas (`ContourGenerationService`),
hillshade/pendiente/aspecto/D8/acumulación/orden Strahler
(`TerrainHydrologyAnalysisService`), drenaje y acondicionamiento
(`DrainageExtractionService`), cuencas por outlet y batch, inundación preliminar
con GeoTIFF, riesgo booleano y multi-amenaza, perfil, recorte DEM, viewshed
implementado y whitebox externo opcional.

Brechas verificadas:

| Brecha | Evidencia |
|---|---|
| Derivados son `BufferedImage` ARGB, no rasters analíticos exportables | `TerrainHydrologyAnalysisService.java:176,200,240,266,301`; `LoadProjectAction.java:257-266` |
| Downsample fijo 256/512/768 | `DrainageExtractionService.java:1495-1498` |
| Umbrales en celdas, no área | `TerrainHydrologyAnalysisService.java:165-169` |
| Cuencas sin morfometría (solo área/orden/clase) | `TerrainHydrologyAnalysisService.java:338-347` |
| Viewshed implementado pero inalcanzable | `TerrainHydrologyAnalysisService.java:2002`; `MainMenuBar.java:551-558` vs `TerrainHydrologyAnalysisDialog.java:40-48` |
| Sin TIN, IDW in-app, kriging, volumen/corte-relleno | grep=0; solo Delaunay 2D (`GeoprocessingService.java:184`) |
| Riesgo con 30 m/111320 hardcodeado, `WILDFIRE` genérico, georref. inventada | `RiskAnalysisEngine.java:65-67`, `:80-96`; `UnifiedAnalysisDialog.java:235-245`, `:368-380` |
| Zonal stats sin llamadores | `ZonalStatisticsEngine.java:12` |
| Hillshade rápido en EDT, capa no persistida | `HillshadeDialog.java:118-146` |

## 2. Arquitectura objetivo

```
catmap/                          CATGIS/
  app/CatmapAppContext            MainMenuBar, comandos
  ui/ (shell, command palette)    analysis/* (motores)
  export/LayoutExportService  <--- layout/LayoutRenderHost (puerto)
  doc/CatmapDocumentIO            layout/LayoutPageSpec
  print/LayoutPrintable
        |                                   |
        v                                   v
  layout/ (elementos, motor)  --->  analitica raster (GeoTIFF, NoData, unidades)
```

Decisiones:
1. **`CatmapAppContext`**: proyecto + host de render + servicios; `catmap.Main`
   no toca estáticos de `CatgisDesktopApp`. `LoadProjectAction` se envuelve en un
   `ProjectOpenService` sin GUI.
2. **`LayoutRenderHost`**: interfaz (proyecto, capas visibles, render de mapa,
   escala) implementada por el host CATGIS y por CATMAP; `LayoutMap`/
   `MapFrameRenderer`/`MapFrameViewport` dejan de leer `AppContext` estático.
   Rompe el ciclo `layout`↔`catmap` con `LayoutElementCodec`.
3. **`LayoutPageSpec`** en el documento: preset o `CUSTOM` mm + orientación +
   márgenes + DPI; la usan preview, export, print y atlas.
4. **`CatmapDocumentIO`**: formato único versionado (migra ambos legados, falla
   explícito si no reconoce; nunca layout vacío silencioso).
5. **`LayoutExportService`**: `renderPage(model, index, pageSpec, metrics)` única
   para PDF multipágina, PNG/JPG/ZIP, SVG vectorial, print (`Book`) y atlas.
6. **`RenderMetrics`**: única conversión pt/mm→px `dpi/72` para todos los
   elementos y etiquetas del mapa; elimina las tres convenciones actuales.
7. **`ScaleResolver`**: escala desde viewport + tamaño de marco + CRS.
8. **Analítica raster**: nuevo pipeline `AnalysisRaster` (float, NoData, unidad,
   estadísticas, GeoTIFF) reutilizando `GeoTiffWriter` (ya en GeoTools) y el
   patrón de `FloodScenarioService.exportScenarioDepthGeoTiff` /
   `DemClipService.clipDem`.
9. **UX**: paleta de comandos, toasts (`NotificationManager` ya existe), salida
   con confirmación universal, HiDPI (iconos multirresolución + escala),
   selector de tema FlatLaf, i18n completa sin traducción automática parcial.
10. **Licencia**: algoritmos implementados desde especificación pública
    (Horn para pendiente/aspecto/hillshade, D8/D∞ para flujo, priority-flood
    para depresiones, Strahler, IDW). Sin código de ArcGIS ni QGIS.

## 3. Fases

| Fase | Contenido | Criterio de salida |
|---|---|---|
| P0 Fundaciones | CatmapAppContext, LayoutRenderHost, LayoutPageSpec, CatmapDocumentIO, LayoutExportService, RenderMetrics | AC-PROD-0/2/3.1–3.3 verdes |
| P1 Cartografía correcta | páginas A3/A4/custom, DPI único, escala derivada, impresión multipágina, paridad preview=export | AC-PROD-2/3.4–3.5/5 |
| P2 Formatos | PDF multipágina, SVG vectorial, GeoTIFF, atlas v2, plantillas standalone | AC-PROD-3.2/4/6/7 |
| P3 Datos | mapa rápido (SHP/GeoJSON/KML/GPKG/GeoTIFF/CSV), WFS/WMS/WMTS, PostGIS opcional | AC-PROD-1 |
| P4 Terreno/hidrología | rasters analíticos, resolución nativa, morfometría, viewshed, perfil, volumen, interpolación, riesgo/inundación calibrados | AC-PROD-8/9 |
| P5 UX | workspace guiado, paleta, toasts, HiDPI, tema, i18n, salida segura | AC-PROD-10 |
| P6 Verificación | A0.2 golden masters + runtime E2E cuando A0.7 lo permita | AC-PROD-11/12 |

## 4. Estrategia de pruebas

- **Golden renders** deterministas (DPI fijo, escena fija) con hashes en `catmap/GoldenImageTest`-style.
- **Paridad preview=export** por geometría y tamaño de texto a 96/150/200 DPI.
- **Roundtrips** de proyecto y documento (A3 vertical, custom mm, multipágina).
- **PDF** parseado con N páginas y tamaño exacto; **GeoTIFF** ida/vuelta con `GeoTiffReader`.
- **Hidrología numérica** con DEM sintético de pendiente conocida, cuenca de área
  conocida, red esperada y tolerancias justificadas.
- **Atlas** con 3 features: extensión, orden, página dinámica y PDF multipágina.
- Todo corre en CI (headless); GUI solo se declara con runtime evidence.

## 5. Riesgos

- **Ratchets** de arquitectura y tests de inventario: cada comando visible nuevo
  debe registrarse en los TSV; extracciones deben crear clases nuevas, no engordar
  hotspots (`MapLayoutComposerDialog` 5625, `LayoutPreviewPanel` 1810, `MapPanel` 3500).
- **A0.7/F3**: el producto se construye con pruebas deterministas; las claims de
  GUI/nativo siguen bloqueadas.
- **Alcance**: no todo QGIS/ArcGIS; se prioriza el flujo "mapa profesional fácil"
  y la hidrología/terreno que el usuario pidió.
- **Datos**: sin acceso al dataset real del usuario; fixtures sintéticos y
  formatos estándar.
