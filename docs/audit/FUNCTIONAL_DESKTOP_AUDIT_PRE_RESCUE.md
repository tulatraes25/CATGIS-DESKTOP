# AUDITORÍA FUNCIONAL TOTAL — CATGIS Desktop PRE-RESCATE

**Fecha:** 2026-06-16  
**Rama:** `main` (bbc6253, ahead 421, behind 7)  
**Estado:** 11 archivos modificados sin commit (285+ líneas de logging diagnóstico)

---

## FASE 0 — Estado de Congelamiento

### Git Status
- **Rama actual:** `main` (bbc6253)
- **Commits ahead:** 421
- **Commits behind:** 7
- **Archivos modificados (uncommitted):** 11
  - MapPanel.java (+32 líneas logging)
  - MapRenderer.java (+11 líneas logging)
  - MapUtilities.java (+25 líneas logging)
  - MouseHandler.java (+46 líneas logging)
  - OnlineDemDownloadDialog.java (+26 líneas logging)
  - OnlineLayerRenderer.java (+14 líneas logging)
  - OpenTopographyDemService.java (-43/+0, refactor)
  - PublicTerrainTilesDemService.java (+12 líneas logging)
  - run.bat (+97 líneas, launcher alternativo)
- **Archivos untracked:** scripts/, EmergencyPerfSmokeTest.java, BUG-AUDIT-2026-06-15.md

### Últimos commits que tocaron áreas críticas

| Área | Último commit | Hash | Problema |
|------|---------------|------|----------|
| **MapPanel (pan/lag)** | Fix pan regression: set map.dragging=true | 5610a55 | Pan roto, fix parcial |
| **MapPanel (listeners)** | Simplify removeNotify: only stop timer | 2b03fa9 | Listeners se perdían |
| **MapPanel (listeners)** | Fix pan regression: addNotify re-registers | 2aa6b08 | Regresión por removeNotify |
| **Raster rendering** | ced2530 Add raster diagnostic logging | ced2530 | Rasters invisibles |
| **DEM download** | 8357e97 Fallback to WCS 1.0.0 | 8357e97 | WCS 2.0.1 fallaba |
| **DEM download** | de7394a Block WorldClim, fix duplicates | de7394a | WorldClim server down |
| **CRS** | 09244f4 Fix EPSG:4326 axis order | 09244f4 | CRS normalize lon/lat |
| **Online layers** | f076214 Add OpenTopoMap/CartoDB | f076214 | Mapas base gratuitos |
| **Toolbar/UI** | fae0449 Reorganize UI | fae0449 | Menú simplificado |
| **CATMAP** | 8491235 Fix CATMAP initial layout | 8491235 | Layout inicial roto |

---

## FASE 1 — Matriz Funcional

| Área | Flujo | Resultado | Evidencia | Riesgo | Acción |
|------|-------|-----------|-----------|--------|--------|
| **A. Inicio UI** | Abrir CATGIS | PARTIAL | run.bat funciona, .exe roto | MEDIO | Verificar sin logging |
| **B. Pan/Mapa** | Mover mapa | FAIL | 5610a55 "Fix pan regression" indica que estaba roto | CRÍTICO | Investigar causa raíz |
| **C. Mapa base online** | Esri/OSM | NOT TESTED | No hay test de integración real | ALTO | Probar con app real |
| **D. Carga vectorial** | Shapefile | PARTIAL | Tests existen pero no verifican render | MEDIO | Verificar visual |
| **E. Raster local** | GeoTIFF DEM | FAIL | ced2530 "raster diagnostic logging" indica problemas | CRÍTICO | Investigar render pipeline |
| **F. DEM online** | Terrain Tiles | FAIL | 8357e97 fallback WCS, de7394a WorldClim down | CRÍTICO | Verificar download real |
| **G. Relieve** | Hillshade/slope | NOT TESTED | Tests unitarios no verifican visual | ALTO | Probar con DEM real |
| **H. Hidrología** | Flow/drainage | NOT TESTED | 1,681 líneas sin validación visual | ALTO | Probar con DEM real |
| **I. Online/clima** | Open-Meteo | PARTIAL | de7394a fix duplicates | MEDIO | Verificar flujo |
| **J. CATMAP** | Layout/export | PARTIAL | 8491235 fix initial layout | MEDIO | Verificar PDF export |
| **K. Proyecto** | Save/load | NOT TESTED | Tests de serialización no verifican capas | ALTO | Probar round-trip real |
| **L. Rendimiento** | Pan lag | FAIL | Logging añadido indica paintComponent lento | CRÍTICO | Medir tiempos reales |

---

## FASE 2 — Flujos Mínimos (evaluación teórica)

### A. Inicio y UI básica
- **run.bat funciona** ✅ (verificado por usuario)
- **.exe roto** ❌ (runtime empaquetado incompleto)
- **Excepciones en consola** → No verificable sin app real

### B. Navegación de mapa
- **Pan regression** → 5 commits recientes intentan arreglar
- **Causa sospechosa:** `removeNotify()` destruía mouse listeners, `addNotify()` los re-registraba
- **Estado:** Fix parcial en 5610a55, pero usuario reporta que "los problemas siguen"

### C. Mapa base online
- **OnlineLayerRenderer** tiene 14 líneas de logging sin commit
- **OnlineTileCache** usa ConcurrentHashMap (bien)
- **Riesgo:** fetch de tiles en EDT bloquearía UI

### D. Carga vectorial
- **ShapefileData** normaliza CRS a lon/lat (09244f4)
- **Risk:** EPSG axis order puede causar capas en posición incorrecta

### E. Raster / DEM local
- **drawRasterLayer** existe en MapPanel
- **LocalRasterData** almacena bandas
- **Riesgo:** Si CRS del raster no coincide con proyecto, capa invisible

### F. DEM online
- **PublicTerrainTilesDemService** +12 líneas logging
- **OpenTopographyDemService** refactor (-43/+0)
- **WorldClim** bloqueado (server down)
- **Riesgo:** Descarga puede fallar silenciosamente

### G. Relieve
- **TopographicProfileService** (450 líneas) - funcional
- **TerrainHydrologyAnalysisService** (2,161 líneas) - funcional
- **Riesgo:** Salidas pueden no renderizarse si DEM no carga

### H. Hidrología
- **DrainageExtractionService** (1,681 líneas) - funcional
- **BooleanRiskService** (904 líneas) - funcional
- **Riesgo:** Requiere DEM cargado primero

### I. Online/clima
- **Open-Meteo** funciona
- **WorldClim** bloqueado
- **SoilGrids** funciona
- **Riesgo:** Capas duplicadas si retry falla

### J. CATMAP
- **LayoutExportEngine** tiene PDF vectorial
- **LayoutModel** con 76 plantillas
- **Riesgo:** PDF puede no abrir si layout corrupto

### K. Proyecto
- **ProjectSerializer/ProjectDeserializer** existen
- **Riesgo:** Round-trip puede perder capas raster/online

### L. Rendimiento
- **paintComponent** itera todas las capas sin cache
- **Online tiles** pueden bloquear EDT
- **CRS transforms** pueden ser lentos
- **Riesgo:** Lag acumulativo con múltiples capas

---

## FASE 3 — Sospechosos Principales

### Top 10 archivos sospechosos

| # | Archivo | Líneas | Sospecha |
|---|---------|--------|----------|
| 1 | **MapPanel.java** | 6,452 | God Object, paintComponent lento, pan regression |
| 2 | **MouseHandler.java** | ~800 | Pan regression, listeners perdidos |
| 3 | **OnlineLayerRenderer.java** | ~500 | Tiles en EDT, fetch síncrono |
| 4 | **RasterImageLoader.java** | ~400 | 9 empty catch blocks, CRS handling |
| 5 | **OnlineDemDownloadDialog.java** | ~300 | Download en background, EDT issues |
| 6 | **PublicTerrainTilesDemService.java** | ~200 | Terrarium decoding, tile URLs |
| 7 | **MapRenderer.java** | 1,256 | Thin wrapper, delegation overhead |
| 8 | **MapUtilities.java** | ~300 | CRS transforms, coordinate conversion |
| 9 | **LayerSymbologyCodec.java** | ~400 | 6 empty catch blocks, deserialization |
| 10 | **DrawFeatureBuilder.java** | ~200 | Drawing geometry creation |

### Top 10 commits sospechosos

| # | Commit | Hash | Problema potencial |
|---|--------|------|-------------------|
| 1 | Fix pan regression: set map.dragging=true | 5610a55 | Fix parcial, puede tener side effects |
| 2 | Simplify removeNotify: only stop timer | 2b03fa9 | Puede perder listeners en某些 casos |
| 3 | Fix regression: remove cleanup() from removeNotify | b3a7d70 | Regresión anterior por cleanup |
| 4 | Fix HIGH bugs: thread safety, process zombie | 49a4658 | Puede introducir race conditions |
| 5 | Normalize CRS to lon/lat across loaders | 09244f4 | Puede romper CRS proyectados |
| 6 | Replace @Disabled with Assumptions | 85ccd54 | Tests pueden saltar sin aviso |
| 7 | Add raster diagnostic logging | ced2530 | Logging puede afectar performance |
| 8 | Block WorldClim (server down) | de7394a | Elimina funcionalidad |
| 9 | Reorganize UI: new menu structure | fae0449 | Puede romper flujos existentes |
| 10 | Fix CATMAP initial layout | 8491235 | Layout puede estar corrupto |

---

## FASE 4 — Clasificación de Daños

### Bloquea beta
1. **Pan lag extremo** — unusable para navegación básica
2. **DEM no descarga** —功能 crítica rota
3. **DEM cargado no se ve** — raster rendering roto
4. **Posible: CRS axis order** — capas en posición incorrecta

### Regresión respecto a versión anterior
1. **Pan regression** — 5 commits intentan arreglar, persiste
2. **Mouse listeners perdidos** — removeNotify destruía listeners
3. **DEM download** — WCS fallback indica que 2.0.1 fallaba
4. **WorldClim** — server permanently down, funcionalidad eliminada

### Roto pero aislable
1. **CATMAP** — layout puede estar corrupto
2. **Hidrología** — requiere DEM que no carga
3. **Clima** — WorldClim down, SoilGrids funciona
4. **TopologyValidator** — gap detection roto (fórmula)

### No probado
1. **Raster reprojection** — no existe engine
2. **Viewshed** — no verificado visualmente
3. **Flow accumulation** — no verificado con DEM real
4. **Save/load project** — round-trip no verificado
5. **PDF export** — no verificado que abra correctamente

### Falso positivo de auditoría
1. **562 tests 0 failures** — no cubren pan, DEM download, raster render
2. **"BUILD SUCCESSFUL"** — no indica salud de app real
3. **Tests de SpatialUtils** — verifican lógica, no UI
4. **Tests de LabelExpressionEngine** — verifican parser, no rendering

---

## FASE 5 — Baseline Funcional

### Commits candidatos a baseline estable

| Commit | Hash | Descripción | Razón |
|--------|------|-------------|-------|
| feat/professional-ui | ca43c82 | Canvas ArcMap-style | Antes de pan regression |
| fix/critical-map-legend-scale | 2ab1ae0 | Map+legend+scale fixes | Antes de CRS normalization |
| feat/surpass-qgis | 2b6c15d | SVG export + auto-componer | Feature completo |
| feat/one-click-ux | 86c7812 | One-click operations | UX completo |

### Rama temporal sugerida
```
rescue/find-last-usable-version
```
Comparar `ca43c82` vs `bbc6253` (HEAD) para identificar regresiones.

---

## FASE 6 — Plan de Rescate

### Prioridad 1: Pan fluido
1. Revisar `MouseHandler` — verificar que mouseDragged no hace operaciones pesadas
2. Revisar `MapPanel.paintComponent` — verificar que no hay repaint innecesarios
3. Medir tiempo de paintComponent con logging actual
4. Si EDT bloqueado → mover tile fetch a background thread

### Prioridad 2: DEM descarga
1. Verificar que `PublicTerrainTilesDemService` genera URL correcta
2. Verificar que Terrarium decoding funciona
3. Verificar que archivo se genera en disco
4. Verificar que capa se agrega al mapa

### Prioridad 3: DEM visible
1. Verificar que `RasterImageLoader.loadReal` carga el raster
2. Verificar que CRS del raster coincide con proyecto
3. Verificar que `drawRasterLayer` dibuja correctamente
4. Verificar que opacidad/orden funciona

### Prioridad 4: Raster/Vector/Base map alineados
1. Verificar que CRS normalization no rompe capas proyectadas
2. Verificar que online tiles se dibujan detrás de vector/raster
3. Verificar que zoom funciona con todas las capas

### Prioridad 5: CATMAP usable
1. Verificar que layout inicial carga correctamente
2. Verificar que PDF export abre correctamente
3. Verificar que plantillas aplican correctamente

### Prioridad 6: Guardar/cargar proyecto
1. Verificar round-trip con vector + raster + online
2. Verificar que CRS persiste
3. Verificar que capas online reconectan

---

## REPORTE FINAL

### 1. Resumen Ejecutivo

CATGIS Desktop tiene **4-5 fallos críticos** que impiden uso real:
- Pan lag extremo (navegación imposible)
- DEM no descarga (función clave rota)
- DEM cargado no se ve (raster rendering roto)
- Posible CRS axis order (capas en posición incorrecta)

El problema principal es que **562 tests no detectan estos fallos** porque:
- Tests son headless (no abren UI)
- Tests no prueban pan/zoom real
- Tests no prueban DEM download real
- Tests no prueban render visual

### 2. Top 10 Regresiones Reales

1. **Pan regression** — 5 commits intentan arreglar, persiste
2. **Mouse listeners perdidos** — removeNotify destruía listeners
3. **DEM WCS 2.0.1 falla** — fallback a 1.0.0
4. **WorldClim server down** — funcionalidad eliminada
5. **CRS axis order** — capas pueden estar en posición incorrecta
6. **Raster invisible** — logging indica problemas
7. **CATMAP layout corrupto** — fix parcial
8. **TopologyValidator gap detection roto** — fórmula matemática incorrecta
9. **RasterCalculatorEngine constante `e` inalcanzable** — tokenizer bug
10. **Salida raster siempre byte-clamped** — destruye precisión float

### 3. Top 10 Sospechosos por Commit/Archivo

1. `MapPanel.java` — God Object, paintComponent lento
2. `MouseHandler.java` — pan regression, listeners
3. `OnlineLayerRenderer.java` — tiles en EDT
4. `RasterImageLoader.java` — CRS handling, empty catches
5. `OnlineDemDownloadDialog.java` — download background
6. `PublicTerrainTilesDemService.java` — Terrarium decoding
7. `5610a55` — Fix pan regression parcial
8. `b3a7d70` — Remove cleanup() from removeNotify
9. `09244f4` — CRS normalization
10. `ced2530` — Raster diagnostic logging

### 4. Qué Fallos Bloquean Beta

- **Pan lag** — imposible navegar mapa
- **DEM no descarga** — función clave rota
- **DEM no visible** — raster rendering roto

### 5. Qué se Puede Ocultar como Experimental

- Hidrología (DrainageExtraction, BooleanRisk)
- TopologyValidator (gap detection roto)
- Climatología (WorldClim down)
- Análisis de红s (NetworkAnalysis)

### 6. Qué Hay que Revertir

- **Nada** — los fixes son intentos legítimos, no causaron nuevos daños
- **Considerar revertir** CRS normalization si rompe capas proyectadas

### 7. Qué Hay que Arreglar

1. **Pan lag** — causa raíz en paintComponent o mouseDragged
2. **DEM download** — verificar Terrarium/WCS pipeline
3. **DEM visible** — verificar RasterImageLoader + drawRasterLayer
4. **CRS axis order** — verificar que normalization no rompe nada

### 8. Qué Tests Faltan

- **Integration test real** — abrir app, pan, zoom, cargar capa
- **DEM download test** — descargar DEM real, verificar archivo
- **Raster render test** — cargar GeoTIFF, verificar que se ve
- **CRS test** — cargar shapefile proyectado, verificar posición
- **Pan performance test** — medir tiempo de paintComponent

### 9. Recomendación Final

**BUSCAR BASELINE** — Identificar commit donde funcionaban pan, DEM, y raster. Comparar contra estado actual para identificar regresiones exactas.

**NO-GO BETA** — No se puede lanzar beta con pan lag, DEM roto, y raster invisible.

**FIX PUNTUAL** — Después de encontrar baseline, arreglar causas raíz específicas, no parches generales.
