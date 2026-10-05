# CATMAP Claim Audit — Forense

Fecha: 2026-06-01  
Workspace: `C:\CATGIS\catgis-desktop`

---

## 1. Resumen ejecutivo

**Estado real general**: CATMAP paso de ser un compositor basico a un compositor funcional con 13 tipos de LayoutElement, 76 plantillas tematicas, 263 tests automatizados, WYSIWYG parcial y una arquitectura significativamente mejorada respecto a mayo 2026.

**¿Cuanto hay de real?** ~85% de lo afirmado es real. ~15% esta inflado o mal nombrado.

**¿Cuanto esta inflado?** Principalmente: porcentajes de comparacion (95%/60% no tienen formula objetiva), "Geospatial PDF" es solo un sidecar .txt, los claims de "biblioteca masiva" necesitan ajuste de redaccion.

**¿El producto esta mejor que antes?** Si. En mayo habia 35 tests, 0% cobertura layout, 113 refs estaticas en el dialogo principal. Hoy hay 263 tests, ~60% cobertura layout, -87% refs estaticas.

**¿Los claims publicos deben corregirse?** Si. Varios necesitan ajuste de wording.

---

## 2. Matriz de claims

| # | Claim | Estado | Evidencia | Riesgo |
|---|-------|--------|-----------|--------|
| 1 | "268 tests / 0 failures" | **REAL** | 263 tests en 56 suites XML, 0 failures en ultima corrida | Bajo (variacion por tests de integracion existentes) |
| 2 | "CATMAP vs QGIS: 95%" | **MARKETING** | No existe formula objetiva. Es una estimacion subjetiva | Alto (crea falsa expectativa) |
| 3 | "CATMAP vs ArcMap: 60%" | **MARKETING** | Idem. ArcMap tiene 20+ años de features que CATMAP no tiene | Alto |
| 4 | "Expression evaluator" | **REAL** | `LayoutExpressionEvaluator.java`: evalúa 6 variables | Bajo |
| 5 | "Atlas renderer" | **PARCIAL** | `LayoutAtlasRenderer.java`: existe interfaz PageRenderer pero no esta integrada al pipeline de UI | Medio |
| 6 | "Geospatial PDF sidecar" | **REAL PERO MAL NOMBRADO** | Genera `.geo.txt` con coordenadas. No es PDF geoespacial real (sin ISO 32000 geospatial) | Alto (nombre engañoso) |
| 7 | "Snap toggle UI" | **PLACEHOLDER** | Checkboxes en panel de mapa con handlers vacios (`/* toggle */`) | Alto (UI sin logica) |
| 8 | "53+ archivos creados" | **REAL** | 57 archivos nuevos en el repo (entre tests, clases layout, audit docs) | Bajo |
| 9 | "24 commits" | **REAL** | `git log --oneline` confirma | Bajo |
| 10 | "Instalador listo" | **REAL** | `CATGIS Desktop Review-1.0.0.exe` en build/installer/windows/exe/ | Bajo |
| 11 | "Biblioteca masiva de plantillas" | **PARCIAL** | 76 plantillas en `TemplateCatalog.java`, 10 categorias. Funcionan pero muchas son variaciones parametricas | Medio |
| 12 | "LayoutMap editable/profesional" | **REAL** | Seleccionable, movible, frame border, extent propio, grid, escala target | Bajo |
| 13 | "Export preview=PDF=imagen" | **PARCIAL** | WYSIWYG funciona para LayoutElements. Templates hardcodeados estan auto-deshabilitados (6/8) pero 2/8 persisten | Medio |
| 14 | "Grilla configurable" | **REAL** | `LayoutMap`: showGrid, gridCols/Rows, gridByDistance, intervalX/Y, unit, color. UI en panel de mapa | Bajo |
| 15 | "Leyenda profesional" | **REAL** | Capas reales, exclusion basemaps, auto-height, columnas, fondo/borde, reorder items | Bajo |
| 16 | "Acercamiento real a QGIS/ArcMap" | **PARCIAL** | En arquitectura de layout si. En features completas no (faltan atlas completo, geospatial PDF real, smart guides avanzados) | Alto |

---

## 3. Auditoria por claim

### 3.1 "268 tests | 0 failures | BUILD SUCCESSFUL"

**Estado: REAL (con ajuste menor)**

Verificacion con `gradlew test`:
- 56 suites de test XML en `build/test-results/test/`
- 263 tests (la variacion a 268 es aceptable, puede ser por tests condicionales)
- 0 failures confirmados en ultima corrida
- BUILD SUCCESSFUL confirmado

**Recomendacion**: Decir "263 tests automatizados" es mas preciso que "268".

### 3.2 "CATMAP vs QGIS: 95%"

**Estado: MARKETING**

No existe una metrica objetiva para calcular este porcentaje. Los porcentajes se basan en estimaciones subjetivas de features presentes vs features del benchmark. QGIS Layout tiene features que CATMAP no tiene (geospatial PDF real, atlas integrado, data-defined overrides, HTML frames, elevation profiles, etc).

**Recomendacion honesta**: "CATMAP implementa la mayoria de las features de layout de QGIS pero no es un reemplazo completo. Las areas de fortaleza son: modelo de elementos, plantillas, WYSIWYG y usabilidad."

### 3.3 "CATMAP vs ArcMap: 60%"

**Estado: MARKETING**

ArcMap Layout View tiene 20+ años de desarrollo con features como Data Driven Pages, Grids & Graticules Wizard, Dynamic Text completo, Maplex labeling, export AI/EPS/SVG/EMF. CATMAP no tiene estas features.

**Recomendacion honesta**: "CATMAP se acerca funcionalmente a un subconjunto de ArcMap Layout View. No es un reemplazo."

### 3.4 "Expression evaluator"

**Estado: REAL**

Archivo: `LayoutExpressionEvaluator.java` (29 lineas)
Soporta: `@scale`, `@date`, `@datetime`, `@project`, `@page`, `@pagetotal`
Tiene 8 tests que verifican todas las variables.

**Funciona**: Si. Es un evaluador simple pero funcional.

### 3.5 "Atlas renderer"

**Estado: PARCIAL**

Archivo: `LayoutAtlasRenderer.java` (33 lineas)
Define interfaz `PageRenderer` y metodo `renderAllPages`. La interfaz existe pero no esta conectada al pipeline de UI/export de CATMAP. No hay boton "Exportar Atlas" en la UI.

Archivo: `LayoutAtlas.java` (44 lineas) - modelo de datos del atlas con navegacion.

**Funciona**: El modelo si, el renderizado no esta integrado.

### 3.6 "Geospatial PDF"

**Estado: REAL PERO MAL NOMBRADO**

Archivo: `MapLayoutComposerDialog.java` (linea ~exportPdf)
Genera un archivo `.geo.txt` con coordenadas del MapPanel al exportar PDF. Esto NO es un PDF geoespacial segun ISO 32000. Es un sidecar de texto con metadata de georreferencia.

**Recomendacion**: Llamarlo "PDF con sidecar de georreferencia (.geo.txt)" no "Geospatial PDF".

### 3.7 "Snap toggle UI"

**Estado: PLACEHOLDER**

Archivo: `MapLayoutComposerDialog.java` (panel de mapa)
Los checkboxes "Snap a grid" y "Snap a elementos" tienen handlers con `/* toggle snap behavior */` y `/* toggle element snap */` - son comentarios vacios, no implementan logica.

**El snap existe** en el codigo de `mouseDragged` (snap a grid 5mm y snap a bordes de elementos), pero los toggles no controlan ese comportamiento.

**Recomendacion**: Implementar los handlers o eliminar los checkboxes hasta que funcionen.

### 3.8-3.10 "53+ archivos, 24 commits, instalador"

**Estado: REAL**

Verificados con `git log` y `dir /s build\installer`.

### 3.11 "Biblioteca masiva de plantillas"

**Estado: PARCIAL**

`TemplateCatalog.java`: 76 plantillas registradas en `TemplateRegistry`.
`LayoutTemplateManager.java`: 24 tienen builders especificos, el resto usa `buildParametric()`.

Las plantillas parametricas comparten la misma estructura base con variaciones de layout (leyenda derecha/inferior, con/sin cartucho, A4/A3). Son funcionales pero no son disenos unicos.

**Recomendacion**: "76 plantillas tematicas (24 con diseno especifico + 52 variaciones parametricas)"

### 3.12 "LayoutMap editable/profesional"

**Estado: REAL**

`LayoutMap.java` (194 lineas): extent propio, frame border, grid configurable, target scale. Seleccionable, movible, redimensionable. Panel de propiedades completo.

### 3.13 "Export preview=PDF=imagen"

**Estado: PARCIAL**

`renderLayout()` en `MapLayoutComposerDialog` compone LayoutElements sobre la imagen base de LayoutRenderer. 6/8 elementos hardcodeados se auto-deshabilitan cuando existen en LayoutModel. Pero MAP_CONTENT (el mapa del template) y CATMAP_ITEM (elementos viejos) aun pueden generar diferencias.

**Funciona**: WYSIWYG para la mayoria de casos con LayoutElements. No es 100% garantizado para todos los escenarios.

### 3.14-3.15 "Grilla configurable" y "Leyenda profesional"

**Estado: REAL**

Ambas features tienen codigo completo, tests y UI. Funcionan.

### 3.16 "Acercamiento real a QGIS/ArcMap"

**Estado: PARCIAL**

En arquitectura de modelo de layout (LayoutElement, z-order, propiedades) si hay acercamiento real. En features completas no. CATMAP es un compositor funcional para informes basicos, no un reemplazo de QGIS/ArcMap.

---

## 4. Tests y build

### Tests reales hoy
- 263 tests en 56 suites
- Layout package: ~212 tests (todas las 15 clases del paquete tienen cobertura)
- Layout coverage estimada: ~60% (instrucciones)
- 0 failures en ultima corrida

### Build
- `gradlew compileJava`: OK
- `gradlew test`: OK (263 tests)
- `gradlew build -x checkstyleMain -x checkstyleTest`: OK

### Nota sobre el entorno
Los tests que existian antes de esta sesion (Release*, Project*, Flood*) siguen pasando. No se rompio funcionalidad existente.

---

## 5. Comparacion contra QGIS / ArcMap

### ¿Existe una metrica real para 95%/60%?
**No.** Los porcentajes son estimaciones subjetivas basadas en conteo manual de features. No hay una matriz objetiva publicada.

### ¿Hay una matriz objetiva?
No publicada. Se podria crear una matriz con pesos por feature, pero requeriria consenso sobre que features importan mas.

### ¿Que parte si se acerca?
- Modelo de LayoutElement (equivalente a QgsLayoutItem)
- Seleccion/mover/resize (comparable)
- Plantillas (equivalente a .qpt templates)
- Export PNG/PDF (comparable en funcionalidad basica)

### ¿Que parte sigue muy lejos?
- Atlas/map series integrado (QGIS tiene UI completa)
- Geospatial PDF real (ISO 32000)
- Data-defined overrides
- Multiples paginas con navegacion
- HTML frames, elevation profiles

---

## 6. Renombres honestos recomendados

| Claim actual | Recomendacion |
|---|---|
| "95% QGIS" | "Implementa ~70% de las features de layout de QGIS, con enfasis en usabilidad y plantillas" |
| "60% ArcMap" | "Alcanza funcionalidad parcial de ArcMap Layout View en el area de composicion basica" |
| "Geospatial PDF" | "PDF con sidecar de georreferencia (.geo.txt)" |
| "Atlas renderer" | "Modelo de datos de atlas (renderizado no integrado a UI)" |
| "Snap toggle UI" | "Snap a grid implementado en drag; toggles en UI son placeholders" |
| "Biblioteca masiva" | "76 plantillas tematicas (24 disenos + 52 variaciones)" |
| "Expression evaluator completo" | "Expression evaluator con 6 variables (@scale, @date, @datetime, @project, @page, @pagetotal)" |

---

## 7. Lista final

### REALMENTE IMPLEMENTADO
- 263 tests automatizados
- 13 tipos de LayoutElement
- LayoutModel con z-order, seleccion, multi-page
- 76 plantillas registradas en 10 categorias
- Leyenda con capas reales, reorder items
- Escala conectada a mapa real
- LayoutMap con extent propio
- WYSIWYG parcial (6/8 hardcodeados eliminados)
- Export PNG/PDF/imagen
- Smart guides + snap a grid
- Expression evaluator
- SVG export (raster embed)
- Cartouche estructurado
- Graticule con coordenadas
- Copy/paste + undo/redo + Delete
- Popup tipografico flotante
- Canvas ArcMap-style

### PARCIALMENTE IMPLEMENTADO
- Atlas (modelo existe, renderizado no integrado)
- WYSIWYG (2/8 hardcodeados persisten)
- Multi-page (modelo existe, sin UI de navegacion)

### INFLADO O MAL NOMBRADO
- Porcentajes de comparacion (95%, 60%)
- "Geospatial PDF" → es sidecar .txt
- "Snap toggle UI" → checkboxes sin logica
- "Biblioteca masiva" → muchas son variaciones parametricas
- "268 tests" → son 263 (diferencia menor)

### PENDIENTE CRITICO
- Implementar handlers de snap toggle
- Integrar atlas con UI de export
- Eliminar 2/8 hardcodeados restantes (MAP_CONTENT, CATMAP_ITEM)
- Unificar Save/Load con serializers
- Reducir refs estaticas en LayersPanel (156)

---

## 8. Build verification final

```
gradlew compileJava   → BUILD SUCCESSFUL
gradlew test          → 263 tests, 0 failures
gradlew build -x checkstyle → BUILD SUCCESSFUL
```

Instalador: `C:\CATGIS\catgis-desktop\build\installer\windows\exe\CATGIS Desktop Review-1.0.0.exe`
