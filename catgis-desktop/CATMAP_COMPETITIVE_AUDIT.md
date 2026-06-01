# CATMAP Competitive Audit — CATGIS Desktop

**Fecha**: 2026-05-31  
**Repositorio**: `C:\CATGIS\catgis-desktop` (branch `master`, commit `ba849ad`)  
**Versión CATGIS**: Desktop Review 1.0.0  

---

## 1. Resumen Ejecutivo

### Estado actual
CATMAP es un compositor cartografico para CATGIS Desktop implementado en Java Swing. Usa un modelo de elementos (`LayoutModel` + `LayoutElement`) que permite agregar, seleccionar, mover y redimensionar objetos en una pagina de layout A4. Soporta exportacion PDF, imagen y carga/guardado en formato `.catmap` (JSON).

### Nivel de madurez
**Prototipo funcional avanzado.** No es producto competitivo todavia.

### Puede competir hoy?
**No.** CATMAP no compite con QGIS Layout Manager ni con ArcMap Layout View. Falta demasiado.

### Que falta para competir
1. LayoutMap no es un map frame vivo (es snapshot cacheado)
2. Elementos cartograficos incompletos (tablas, graticulas, etiquetas dinamicas, cartucho estructurado)
3. Panel de elementos no profesional
4. Sin atlas/map series
5. Sin SVG export
6. Sin multiples paginas
7. Sin graticulas coordenadas reales
8. Sin plantillas intercambiables con otros GIS

### Riesgos principales
- La arquitectura Swing limita el renderizado vectorial avanzado
- LayoutModel y sistema de plantillas hardcodeado coexisten con paths de render separados
- Los elementos CatmapLayoutItem (sistema viejo) y LayoutElement (sistema nuevo) coexisten sin integracion completa
- No hay test automatizado de regresion visual

---

## 2. Tabla Comparativa General

### ARQUITECTURA

| Feature | CATMAP | QGIS | ArcMap | ArcGIS Pro | Prioridad |
|---|---|---|---|---|---|
| Layout model | LayoutModel + LayoutElement (parcial) | QgsLayout + QgsLayoutItem | Internal COM | CIMLayout | **Critico** |
| Fuente unica de verdad | LayoutModel + hardcoded template conviven | Si | Si | Si | **Critico** |
| Export pipeline unico | Parcial (renderLayout compone, pero drawFooter/drawHeader son hardcodeados) | Si (QgsLayoutExporter) | Si | Si | **Alto** |
| Plantillas guardado/carga | .catmap JSON | .qpt XML | .mxt | .pagx | **Critico** |
| Import/Export layouts | QPT importer basico | QPT + templates | MXD | PAGX | **Medio** |

### INTERACCION

| Feature | CATMAP | QGIS | ArcMap | ArcGIS Pro | Prioridad |
|---|---|---|---|---|---|
| Seleccionar elemento | OK (z-order, tolerancia 5mm) | OK | OK | OK | **Hecho** |
| Mover elemento | OK (LayoutModel drag) | OK | OK | OK | **Hecho** |
| Redimensionar (handles) | OK (8 handles, resize con isSelected guard) | OK | OK | OK | **Hecho** |
| z-order | OK (moveUp/Down/Front/Back con normalizeZOrder) | OK | OK | OK | **Hecho** |
| Bloquear elemento | OK | OK | No | OK | **Hecho** |
| Ocultar elemento | OK | OK | Parcial | OK | **Hecho** |
| Alinear | OK (6 modos) | OK (6 modos) | OK | OK (Align to Page extra) | **Hecho** |
| Distribuir | Parcial | OK | OK | OK (Make Same Size extra) | **Medio** |
| Reglas | OK (mm marks top/left) | OK | OK | OK | **Hecho** |
| Guias | OK (draggable blue lines) | OK | OK | OK | **Hecho** |
| Snap | Parcial (solo guias) | OK (grid + smart guides) | OK | OK | **Medio** |
| Undo/Redo | OK (undoStack) | OK | OK | OK | **Hecho** |
| Modo dibujo directo | OK (click-drag rectangulo/elipse/linea) | OK | OK | OK | **Hecho** |
| Hover feedback | OK (borde punteado azul) | OK | OK | OK | **Hecho** |
| Popup tipografico | OK (flotante con fuente/tamano/negrita/color) | Viene del panel Item Properties | Viene de dialogos | Viene del Format pane | **Hecho** |
| Click derecho contextual | OK (12 opciones) | OK | OK | OK | **Hecho** |

### MAPA (Map Frame)

| Feature | CATMAP | QGIS | ArcMap | ArcGIS Pro | Prioridad |
|---|---|---|---|---|---|
| Map frame vivo | **NO** (snapshot cacheado del MapPanel) | Si (QgsLayoutItemMap) | Si (Data Frame) | Si (Map Frame) | **CRITICO** |
| Extent propio por frame | **NO** (todos muestran el mismo viewport) | Si | Si | Si | **CRITICO** |
| Escala independiente | **NO** | Si | Si | Si | **CRITICO** |
| CRS propio por frame | **NO** | Si | Si | Si | **Medio** |
| Bloquear extent | **NO** | Si (Lock layers) | Si | Si | **Alto** |
| Pan/zoom dentro del marco | OK (Pan mapa tool, doble clic activa) | OK (Move Item Content) | OK | OK | **Hecho** |
| Multiples mapas en layout | **NO** (solo uno via template MAP_CONTENT) | Si (ilimitados) | Si | Si | **Alto** |
| Grilla/graticula | Parcial (filas/columnas, distancia X/Y) | OK (multiple grids, CRS propio, coordenadas, ticks) | OK (3 tipos) | OK | **Alto** |
| Overview/inset map | **NO** | OK (multiple overviews per map) | Si (Extent Indicators) | Si | **Bajo** |
| Rotacion de mapa | **NO** | OK | OK | OK | **Medio** |
| Capas por frame | **NO** (misma visibilidad global) | Si (lock layers, map themes) | Si | Si | **Alto** |

### LEYENDA

| Feature | CATMAP | QGIS | ArcMap | ArcGIS Pro | Prioridad |
|---|---|---|---|---|---|
| Capas reales | OK (populateLegendFromProject) | OK | OK | OK | **Hecho** |
| Excluir mapas base | OK (isBasemapName) | Manual | Manual | Manual | **Hecho** |
| Simbolos reales | Parcial (circulo/cuadrado/diamante/triangulo por geometria) | OK (symbol patches) | OK | OK | **Alto** |
| Fuentes configurables | OK (titulo, items) | OK | OK | OK | **Hecho** |
| Columnas | OK (setColumns) | OK | OK | OK | **Hecho** |
| Auto-height | OK (setAutoHeight) | OK (Resize to fit) | Si | Si | **Hecho** |
| Word wrap | OK (wrapLines) | OK | Si | Si | **Hecho** |
| Orden manual items | **NO** | OK (drag-drop tree) | Si | Si | **Medio** |
| Renombrar items | **NO** | OK | Si | Si | **Medio** |
| Grupos | **NO** | OK | Si | Si | **Bajo** |
| Catalogo de simbolos | Parcial (PointSymbolCatalog con 50+ entries, render Java2D) | OK (SVG library + custom) | OK | OK (Style gallery) | **Alto** |
| Fondo/Borde/Opacidad | OK | Si | Si | Si | **Hecho** |

### ELEMENTOS CARTOGRAFICOS

| Feature | CATMAP | QGIS | ArcMap | ArcGIS Pro | Prioridad |
|---|---|---|---|---|---|
| Escala grafica | OK (LayoutScaleBar, calculo desde mapScaleDenominator) | OK (6 estilos) | OK | OK | **Hecho** |
| Norte | OK (LayoutNorthArrow, triangulo relleno + N) | OK (SVG image, sync rotation) | OK | OK | **Hecho** |
| Texto libre | OK (LayoutLabel, fuente/tamano/color) | OK (Label + Dynamic Text) | OK | OK | **Hecho** |
| Imagen/Logo | OK (LayoutImage, BufferedImage) | OK (Picture) | OK | OK | **Hecho** |
| Rectangulo | OK (LayoutRectangle + draw mode) | OK | OK | OK | **Hecho** |
| Elipse | **NO** (usa LayoutRectangle como fallback) | OK | OK | OK | **Medio** |
| Linea | Parcial (usa LayoutRectangle como fallback) | OK (Polyline con nodos) | OK | OK | **Medio** |
| Cartucho | Parcial (LayoutLabels para datos, drawFooter hardcodeado como fallback) | Si (via labels + dynamic text) | Si | Si | **Alto** |
| Datos dinamicos | **NO** (todo es texto estatico) | OK (expressions, variables, atlas) | OK (dynamic text) | OK | **Medio** |
| Tablas | OK (LayoutTable CSV) | OK (Attribute Table + Fixed Table) | No nativo | OK (Table frames) | **Alto** |
| Graficos/Charts | **NO** | **NO** (elevation profile parcial) | Parcial | OK (Chart frames) | **Bajo** |
| HTML frame | **NO** | OK | No | No | **Bajo** |

### EXPORTACION

| Feature | CATMAP | QGIS | ArcMap | ArcGIS Pro | Prioridad |
|---|---|---|---|---|---|
| PDF | OK (renderLayout + PDFBox) | OK | OK | OK | **Hecho** |
| Imagen PNG | OK (renderLayout) | OK (PNG/JPG/TIFF/BMP...) | OK | OK | **Hecho** |
| SVG | **NO** | OK | OK (ArcMap) | **NO** (Pro) | **Medio** |
| Impresion | OK (printLayout) | OK | OK | OK | **Hecho** |
| WYSIWYG | Parcial (DPI preview fijo 200, export usa settings.dpi; leyenda auto-disable en preview) | OK | OK | OK | **Alto** |
| Geospatial PDF | **NO** | OK | OK | OK | **Bajo** |
| World file | **NO** | OK | OK | OK | **Bajo** |
| Multipage | **NO** | OK (atlas + pages) | OK (DDP) | OK (Map Series) | **Medio** |

### PLANTILLAS

| Feature | CATMAP | QGIS | ArcMap | ArcGIS Pro | Prioridad |
|---|---|---|---|---|---|
| Guardar plantilla | OK (.catmap JSON con todos los campos) | OK (.qpt) | OK (.mxt) | OK (.pagx) | **Hecho** |
| Cargar plantilla | OK | OK | OK | OK | **Hecho** |
| Plantillas predisenadas | OK (6: ambiental, tecnico, A3, muestreo, satelital, vertical) | OK | OK | OK | **Hecho** |
| Importar QGIS QPT | Parcial (QgisQptImporter: labels, maps, legend, scale, north, rect) | N/A | N/A | N/A | **Alto** |
| Importar MXD | **NO** | Parcial (via plugin) | N/A | N/A | **Bajo** |
| Formato propio | .catmap JSON | .qpt XML | .mxt binary | .pagx JSON | **Hecho** |

### PROFESIONAL

| Feature | CATMAP | QGIS | ArcMap | ArcGIS Pro | Prioridad |
|---|---|---|---|---|---|
| Interfaz | Java Swing, FlatLaf, panel izquierdo con dropdowns | Qt, dock widgets | MFC/Win32 | WPF/.NET | **Alto** |
| Panel elementos | OK (iconos tipo, nombres amigables, secciones) | OK (Items panel) | OK (TOC) | OK (Contents pane) | **Hecho** |
| Panel propiedades | Parcial (card layout para Map/Legend/Label, generic fallback) | OK (Item Properties) | OK (dialogos) | OK (Element + Format pane) | **Alto** |
| Flujo de trabajo | Parcial (seleccionar default, pero toolbar confusa con 2 sistemas) | OK | OK | OK | **Medio** |
| Estabilidad | OK (compila, build pasa) | OK | OK | OK | **Hecho** |

---

## 3. Diagnostico de Brecha

### CRITICO (bloquea uso profesional)

1. **LayoutMap NO es map frame vivo** — usa snapshot cacheado del MapPanel global. Todos los LayoutMaps muestran lo mismo. No tiene extent/escala/capas propias. El pan interno (Pan mapa) funciona para mover contenido, pero el mapa no es un frame independiente.

2. **Dos sistemas de render coexisten** — `LayoutRenderer.renderResult()` (hardcoded: drawHeader, drawMapFrame, drawFooter, drawGrid, drawNorthArrow, drawScaleBar, drawLegend) vs `drawLayoutModelOverlay()` (LayoutElements del modelo). El auto-disable en paintComponent mitiga duplicados, pero el codigo hardcodeado sigue ahi.

3. **CatmapLayoutItems vs LayoutElements** — El toolbar viejo inserta CatmapLayoutItems (texto, imagen, rectangulo, elipse, linea) gestionados por `interactionState`. Los nuevos LayoutElements usan `draggingLayoutElement` en `mouseDragged`. Dos sistemas de interaccion paralelos.

4. **Sin soporte para multiples mapas** — Solo un MAP_CONTENT por template. LayoutMap en el modelo se saltea en el overlay.

### ALTO (limita calidad profesional)

5. **Export WYSIWYG incompleto** — Preview usa `legendCheck.setSelected(false)` para evitar doble leyenda. El drawHeader/drawFooter hardcodeado aun se renderizan si el modelo no tiene titulo/subtitulo/cartouche.

6. **QPT importer basico** — Soporta ComposerLabel, ComposerMap, ComposerLegend, ComposerScaleBar, ComposerNorthArrow, ComposerPicture. No soporta grillas, overviews, atlas, HTML frames, tablas, polilineas, poligonos.

7. **Grilla sin coordenadas reales** — Las etiquetas son A, B, C... y 1, 2, 3... no coordenadas geograficas. Sin soporte para graticulas (lat/lon) ni CRS propio de grilla.

8. **LayoutTable solo CSV** — Sin soporte para XLSX, XLS, ODS. Sin seleccion de hoja/rango.

9. **Simbolos de leyenda limitados** — El renderSymbol dibuja formas geometricas basicas. No usa los estilos reales de la capa (grosor de linea, patron de relleno, marker SVG).

10. **Cartucho no estructurado** — Los datos del cartucho (estudio, empresa, cartografo, fuente, CRS) son LayoutLabels separados. No hay un LayoutCartouche que agrupe los 6 campos con formato de tabla.

### MEDIO (mejora significativa)

11. **DPI preview fijo 200** vs export variable (150/200/300). Las proporciones son correctas (mmToPx adapta), pero la calidad del preview difiere de la exportacion.

12. **Sin snap a grid** — Solo hay snap a guias. QGIS tiene snap a grid, smart guides, y snap a bordes de elementos.

13. **Sin elipse/linea propias** — LayoutRectangle se usa como fallback. No hay Path2D para dibujar elipses reales ni lineas con grosor.

14. **Sin grupos de elementos** — QGIS/ArcMap permiten agrupar elementos para moverlos juntos.

15. **Sin atlas/map series** — No hay iteracion sobre entidades para generar series de mapas.

### BAJO (nice to have)

16. **Sin SVG export** — Solo PNG y PDF (via PDFBox como imagen rasterizada, no vectorial).
17. **Sin geospatial PDF** — El PDF exportado no tiene georreferenciacion.
18. **Sin multiples paginas** — Layout fijo de una pagina.
19. **Sin temas visuales** — FlatLaf es el unico tema.
20. **Sin graficos/charts** — No hay elementos de grafico.

---

## 4. Comparacion Honesta

### CATMAP hoy puede competir con QGIS Layout?
**No.** QGIS Layout tiene: map frame vivo con extent/escala/capas/CRS propios, graticulas con coordenadas reales, leyenda con tree de items ordenables, atlas/map series, SVG export, geospatial PDF, data-defined overrides, reportes, variables, plantillas .qpt, smart guides con snap a grid y bordes, polilineas con nodos editables, tablas dinamicas desde capas, elevation profiles, HTML frames, y 18 tipos de layout items.

CATMAP tiene: seleccion/mover/resize confiable, leyenda basica, escala grafica, norte, texto, imagen, tabla CSV, 6 plantillas, export PNG/PDF. Es un 20% de QGIS Layout.

### CATMAP hoy puede competir con ArcMap Layout View?
**No.** ArcMap Layout View tiene: Data Frame como map frame vivo, Grids & Graticules Wizard, Legend Wizard, scale bar con estilos, extent indicators, dynamic text, data driven pages, export multi-formato, .mxt templates, y 20+ años de refinamiento.

CATMAP estaria a nivel de un visor de mapas con capacidades basicas de composicion, no un compositor cartografico.

### CATMAP hoy puede competir con ArcGIS Pro Layout?
**No.** ArcGIS Pro Layout tiene todo lo de ArcMap mas: map series (3 tipos), chart frames, table frames, style gallery, layout files .pagx, Contents pane, Format pane, map frame constraints, blend modes, 3D scenes en layout, y una arquitectura moderna WPF/.NET.

### CATMAP puede servir para informes ambientales basicos?
**Si, parcialmente.** Puede generar un layout A4 con mapa, leyenda, escala, norte, titulo y subtitulo, exportar a PNG y PDF. Es suficiente para un informe estudiantil o interno basico. Pero no para un informe profesional que requiera: graticulas con coordenadas, multiples mapas de detalle, tablas de datos vinculadas a capas, cartucho estructurado con metadata, o atlas de series.

### Que necesita para verse profesional?
1. Eliminar completamente los elementos hardcodeados (drawHeader, drawFooter, drawLegend, drawNorthArrow, drawScaleBar en LayoutRenderer) y migrarlos a LayoutElements.
2. LayoutMap como map frame vivo independiente del MapPanel global.
3. Panel de propiedades unificado para todos los tipos de LayoutElement.
4. Graticulas con coordenadas geograficas reales.
5. Tabla vinculada a capa (atributos de features).
6. Cartucho estructurado como LayoutElement compuesto.
7. Eliminar CatmapLayoutItems (sistema viejo) y unificar todo en LayoutElements.

### Que necesita para ser tecnicamente confiable?
1. Tests automatizados de regresion visual (compara render preview vs export).
2. Pipeline de render unico (un solo metodo que sirva para preview, PNG, PDF, print).
3. LayoutModel como unica fuente de verdad (eliminar interactionState para posiciones).
4. Validacion de .catmap al cargar (schema, version).

### Que features conviene copiar primero? (orden de prioridad)
1. Map frame vivo (de QGIS/ArcMap) — CRITICO
2. Graticulas con coordenadas (de QGIS) — ALTO
3. Panel de propiedades unificado (de ArcGIS Pro Format pane) — ALTO
4. Tabla vinculada a capa (de ArcGIS Pro Table frame) — ALTO
5. Atlas/map series (de QGIS) — MEDIO
6. Smart guides + snap a bordes (de QGIS) — MEDIO
7. SVG export (de QGIS) — MEDIO
8. LayoutElipse, LayoutLine (de QGIS polylines) — MEDIO

### Que features NO conviene intentar todavia?
- Geospatial PDF (requiere motor GIS con proyeccion)
- HTML frames (requiere motor de rendering web)
- 3D map frames (arquitectura completamente diferente)
- Elevation profiles (requiere datos DEM + procesamiento)
- Importar MXD (formato binario propietario de Esri, sin documentacion publica)

---

## 5. Recomendacion de Arquitectura

### Arquitectura objetivo

```
LayoutModel (fuente unica de verdad)
  └── List<LayoutElement>
        ├── LayoutMap (map frame vivo con extent/escala/capas/CRS propios)
        ├── LayoutLegend (leyenda con items desde capas)
        ├── LayoutScaleBar (escala grafica vinculada a LayoutMap)
        ├── LayoutNorthArrow (norte vinculado a rotacion de LayoutMap)
        ├── LayoutLabel (texto libre con formato)
        ├── LayoutImage (imagen desde archivo/BufferedImage)
        ├── LayoutRectangle (rectangulo)
        ├── LayoutEllipse (elipse)
        ├── LayoutLine (linea con grosor)
        ├── LayoutTable (tabla desde CSV/capa)
        ├── LayoutCartouche (cartucho estructurado: estudio, empresa, etc.)
        └── LayoutGraticule (graticula vinculada a LayoutMap)

LayoutRenderer (pipeline unico)
  └── render(LayoutModel, settings) → BufferedImage
       ├── usado por: preview, exportImage, exportPdf, printLayout
       └── WYSIWYG garantizado

LayoutTemplateManager
  └── saveTemplate / loadTemplate / applyTemplate → .catmap JSON

QgisQptImporter
  └── importQpt(File) → ImportResult con pagina + LayoutElements

CanvasDropTarget
  └── drag & drop imagenes/tablas al canvas
```

### Reglas de arquitectura

1. **Todo visible = LayoutElement.** Nada de drawHeader/drawFooter/drawLegend hardcodeados.
2. **LayoutModel es la unica fuente de verdad.** interactionState solo para estado transitorio (seleccion, tool activo), no para posiciones.
3. **Un solo pipeline de render.** `LayoutRenderer.render(model, settings)` para preview + export + print.
4. **CatmapLayoutItems eliminados.** Migrar a LayoutElements (LayoutLabel, LayoutImage, LayoutRectangle, LayoutEllipse, LayoutLine).
5. **LayoutMap independiente.** Cada LayoutMap tiene su propio extent, escala, capas, CRS. No depende de CatgisDesktopApp.mapPanel global.

---

## 6. Roadmap por Fases

### Fase A — Estabilizacion (2-3 sprints)
**Objetivo**: CATMAP usable sin errores de interaccion.

- [x] Eliminar leyenda fantasma (double-render)
- [x] Seleccion/mover/resize confiable (z-order, tolerancia 5mm, resize solo si selected)
- [x] Panel elementos con nombres amigables e iconos de tipo
- [x] Toolbar renombrada (sin abreviaturas)
- [x] Seleccionar como modo default
- [x] Ghost map en pan eliminado
- [x] Doble clic en LayoutMap activa pan interno
- [x] Doble-division lastPreviewScale corregida en 5 handlers
- [x] Popup tipografico flotante (reemplaza JOptionPane)
- [x] Dibujo directo de formas (sin dialogo inicial)
- [ ] **PENDIENTE**: Migrar CatmapLayoutItems a LayoutElements
- [ ] **PENDIENTE**: Eliminar codigo viejo de interaccion (interactionState para posiciones)

### Fase B — Elementos Cartograficos (2-3 sprints)
**Objetivo**: LayoutElements completos para composicion profesional.

- [x] LayoutLegend con capas reales, auto-height, word-wrap, columnas
- [x] LayoutScaleBar con calculo desde mapScaleDenominator
- [x] LayoutNorthArrow
- [x] LayoutTable CSV
- [ ] **PENDIENTE**: LayoutCartouche estructurado
- [ ] **PENDIENTE**: LayoutEllipse (Path2D real)
- [ ] **PENDIENTE**: LayoutLine (Path2D con grosor y estilo)
- [ ] **PENDIENTE**: LayoutGraticule con coordenadas reales
- [ ] **PENDIENTE**: LayoutTable XLSX/ODS + vinculacion a capa

### Fase C — Pipeline Unico (1 sprint)
**Objetivo**: WYSIWYG garantizado en preview + export + print.

- [x] renderLayout compone LayoutModel en export
- [x] DPI de settings usado en renderLayout
- [ ] **PENDIENTE**: Eliminar drawHeader/drawFooter/drawLegend/drawNorthArrow/drawScaleBar de LayoutRenderer
- [ ] **PENDIENTE**: Migrar MAP_CONTENT del template a LayoutMap independiente
- [ ] **PENDIENTE**: LayoutRenderer.render(model, settings) como metodo unico

### Fase D — Plantillas e Importadores (1-2 sprints)
**Objetivo**: Intercambio de layouts con otros GIS.

- [x] .catmap JSON con todos los campos (save/load)
- [x] 6 plantillas predisenadas
- [x] QPT importer basico (labels, maps, legend, scale, north, rect)
- [ ] **PENDIENTE**: QPT importer avanzado (grids, overviews, polylines, HTML frames)
- [ ] **PENDIENTE**: Validacion de schema .catmap al cargar
- [ ] **PENDIENTE**: Compatibilidad hacia atras (layouts viejos sin campos nuevos)

### Fase E — Profesionalizacion (3-4 sprints)
**Objetivo**: Competir con QGIS Layout.

- [ ] LayoutMap como map frame vivo (extent/escala/capas/CRS propios)
- [ ] Multiples LayoutMaps en un layout
- [ ] Atlas/map series
- [ ] SVG export
- [ ] Catalogo de simbolos completo (SVG real, no solo Java2D)
- [ ] Smart guides + snap a bordes de elementos
- [ ] Grupos de elementos
- [ ] Data-defined overrides (expresiones para texto dinamico)

---

## 7. Analisis de MXD / ArcMap

### Es viable leer MXD directamente?
**Tecnicamente**: MXD es un formato binario OLE Structured Storage (COM). Esri no publica su especificacion. Existen proyectos open source (p.ej. `mxd2map` en Python) que hacen ingenieria inversa parcial, pero son incompletos e inconsistentes entre versiones.

**Legalmente**: Los MXDs contienen datos propietarios de Esri. La ingenieria inversa de formatos binarios sin documentacion publica tiene riesgos legales en algunas jurisdicciones.

### Existe libreria open source confiable?
**No.** Ninguna libreria open source lee MXDs de forma completa y confiable. Los intentos existentes (ArcMapReader de GeoTools, mxd2qgs) son parciales: leen titulo, extent del data frame, paths de capas. No leen simbologia, layouts, ni elementos graficos.

### Conviene apuntar a QPT primero?
**Si.** QPT es XML abierto y documentado. QGIS es open source (GPL), lo que permite estudiar su codigo para entender el formato. El QPT importer ya existe en CATMAP (basico) y puede expandirse.

### Conviene importar PDF/SVG exportado?
**No.** PDF y SVG exportados desde ArcMap/QGIS pierden la estructura de capas, la georreferenciacion y la capacidad de editar elementos individualmente. Son formatos de salida, no de intercambio.

### Conviene crear un transformador externo?
**Si, para MXD → QPT.** Un script externo (Python + arcpy si hay licencia ArcMap disponible, o QGIS + mxd2qgs) que convierta MXD a QPT. Luego CATMAP importa el QPT. Esto separa la dependencia de Esri del codigo de CATGIS.

### Alternativa realista propuesta
1. **Priorizar QPT importer** (formato abierto, QGIS es GPL — copiar patrones de arquitectura, NO codigo)
2. **Para MXD**: ofrecer un convertidor externo opcional (Python script) que requiera ArcMap instalado
3. **Para PAGX** (ArcGIS Pro): formato JSON documentado. Investigar viabilidad futura.

---

## 8. Auditoria de Codigo CATMAP

### Archivos revisados

| Archivo | Lineas | Estado |
|---|---|---|
| `MapLayoutComposerDialog.java` | ~8600 | Monolito con LayoutRenderer, LayoutInteractionState, LayoutPreviewPanel, 2 sistemas de interaccion |
| `layout/LayoutModel.java` | ~110 | OK. Fuente de verdad parcial |
| `layout/LayoutElement.java` | ~24 | Interfaz limpia. 15 metodos. |
| `layout/LayoutMap.java` | ~160 | NO es map frame vivo. Snapshot cacheado del MapPanel global |
| `layout/LayoutLegend.java` | ~297 | OK. Capas reales, auto-height, columnas, fondo/borde |
| `layout/LayoutScaleBar.java` | ~100 | OK. Calculo desde mapScaleDenominator |
| `layout/LayoutNorthArrow.java` | ~70 | OK. Triangulo relleno + N |
| `layout/LayoutLabel.java` | ~63 | OK. Texto, fuente, color |
| `layout/LayoutImage.java` | ~53 | OK. BufferedImage |
| `layout/LayoutRectangle.java` | ~60 | OK. Rectangulo con relleno y borde |
| `layout/LayoutTable.java` | ~180 | OK. CSV reader con encabezados y filas alternadas |
| `layout/LayoutTemplateManager.java` | ~300 | OK. 6 plantillas, save/load JSON |
| `layout/QgisQptImporter.java` | ~130 | Basico. 6 tipos de elementos |
| `layout/CanvasDropTarget.java` | ~80 | OK. Drag & drop imagenes |
| `layout/GuideLine.java` | ~60 | OK. Guias azules arrastrables |
| `layout/RulerRenderer.java` | ~100 | OK. Marcas mm en bordes |
| `layout/LayoutRenderContext.java` | ~37 | OK. mmToPx/pxToMm |

### Metodos problematicos

| Metodo | Archivo | Linea | Problema |
|---|---|---|---|
| `LayoutRenderer.renderResult()` | MapLayoutComposerDialog | ~5624 | Renderiza drawHeader, drawMapFrame, drawFooter, drawGrid, drawNorthArrow, drawScaleBar, drawLegend hardcodeados fuera del LayoutModel |
| `drawHeader()` | MapLayoutComposerDialog | ~5896 | Titulo, subtitulo, metadata hardcodeados |
| `drawFooter()` | MapLayoutComposerDialog | ~6547 | Cartouche hardcodeado |
| `drawLegend()` | MapLayoutComposerDialog | ~6232 | Leyenda hardcodeada (auto-disabled por legendCheck) |
| `LayoutMap.captureMapImage()` | LayoutMap.java | ~102 | Usa MapPanel global, no extent propio |
| `interactionState.translate()` | MapLayoutComposerDialog | ~4238 | Sistema viejo de offsets para CatmapLayoutItems |

### Renders hardcodeados (fuera de LayoutModel)

1. `drawHeader` → `LayoutElementType.HEADER`
2. `drawMapFrame` → `LayoutElementType.MAP_CONTENT`
3. `drawFooter` → `LayoutElementType.CARTOUCHE`
4. `drawLegend` → `LayoutElementType.LEGEND`
5. `drawNorthArrow` → `LayoutElementType.NORTH`
6. `drawScaleBar` → `LayoutElementType.SCALE`
7. `drawGrid` → via `settings.showGrid()`
8. `drawCatmapItems` → `LayoutElementType.CATMAP_ITEM`

**8 de 8 hardcodeados.** El auto-disable (legendCheck.setSelected, interactionState.setElementVisible) mitiga pero no elimina.

### Puntos donde exportacion no coincide con preview

- Preview llama `drawLayoutModelOverlay()` (linea ~5721). Export NO (linea ~3493 `renderLayout`).
- FASE 2 (commit `872760a`) agrego composicion de LayoutModel en `renderLayout`, pero solo para LayoutElements individuales (no MAP_CONTENT, no HEADER, no CARTOUCHE).
- `drawLayoutModelOverlay` en preview skipea LayoutMap cuando `templateHasMap=true`.

### Riesgos de duplicidad de leyenda

- **Mitigado**: `legendCheck.setSelected(false)` en paintComponent antes de buildSettings.
- **Riesgo residual**: Si el modelo NO tiene LayoutLegend pero el usuario marco legendCheck manualmente, se renderiza la leyenda hardcodeada (esperado: es la unica leyenda).

### Bugs de seleccion

- **Corregidos**: Doble-division lastPreviewScale en 5 handlers (FASE 1, commit `c08a69f`).
- **Corregido**: Resize requiere isSelected() previo (FASE 1).
- **Corregido**: hitTestHandle en page-pixels (FASE 1).

### Bugs de move/resize

- **Corregido**: resizeElement usaba doble-division (FASE 1).
- **Corregido**: mouseDragged delta usa pxToMmScale correcto.

### Puntos a refactorizar

1. **Eliminar `interactionState` para posiciones de elementos.** Las posiciones deben venir del LayoutModel.
2. **Unificar `mouseDragged`**: un solo path para mover LayoutElements (eliminar path viejo de CatmapLayoutItems).
3. **Extraer LayoutRenderer a archivo separado** (layout/LayoutRenderer.java) para mantener MapLayoutComposerDialog bajo 4000 lineas.
4. **Migrar CatmapLayoutItems a LayoutElements** (LayoutLabel, LayoutImage, LayoutRectangle, LayoutEllipse, LayoutLine).
5. **Crear LayoutCartouche** como elemento compuesto con 6 campos predefinidos.
6. **Refactorizar LayoutMap** para que cada instancia tenga su propio extent/escala/capas.

---

## 9. Conclusion

### Que tan lejos esta CATMAP?
CATMAP esta a un **30%** de QGIS Layout Manager y a un **20%** de ArcMap Layout View en terminos de funcionalidad. Tiene los fundamentos (modelo de elementos, seleccion/mover/resize, export basico, leyenda) pero le falta el 70% de las features de un compositor profesional.

### Que hay que hacer primero?
**Eliminar el codigo hardcodeado.** Mientras existan `drawHeader`, `drawFooter`, `drawLegend`, `drawNorthArrow`, `drawScaleBar` en `LayoutRenderer.renderResult()`, CATMAP nunca sera WYSIWYG ni tendra una arquitectura limpia. Todo debe ser LayoutElement en el modelo.

Segundo: **LayoutMap como map frame vivo.** Sin esto, CATMAP es un visor de mapas con adornos, no un compositor.

### Que NO debe hacerse?
- No intentar soportar MXD directamente (inviable tecnica y legalmente).
- No agregar features cosméticas (temas, animaciones) antes de arreglar la arquitectura.
- No crear mas elementos hardcodeados fuera del LayoutModel.
- No tocar el motor GIS/CRS (esta fuera del alcance de CATMAP).

### Conviene reescribir CATMAP o refactorizar por partes?
**Refactorizar por partes.** CATMAP tiene ~8600 lineas en un archivo. Una reescritura completa tomaría meses y romperia compatibilidad. Pero refactorizar incrementalmente:

1. Extraer `LayoutRenderer` a `layout/LayoutRenderer.java` (~300 lineas)
2. Migrar CatmapLayoutItems a LayoutElements (~200 lineas)
3. Eliminar `interactionState` para posiciones (~100 lineas)
4. Crear `LayoutCartouche` (~150 lineas)
5. Hacer `LayoutMap` independiente (~300 lineas)

Esfuerzo estimado: **medio** (2-3 meses, 1 desarrollador).

### Estimacion relativa de esfuerzo

| Tarea | Esfuerzo |
|---|---|
| Estabilizacion (Fase A pendientes) | Chico (1-2 semanas) |
| LayoutMap vivo | Grande (1 mes) |
| Pipeline unico (eliminar hardcodeados) | Medio (2-3 semanas) |
| LayoutCartouche + LayoutEllipse + LayoutLine | Chico (1 semana) |
| Graticulas con coordenadas | Medio (2 semanas) |
| QPT importer avanzado | Medio (2-3 semanas) |
| Atlas/map series | Grande (1-2 meses) |
| SVG export | Medio (2 semanas) |
| Multiples paginas | Grande (1 mes) |
| Catalogo de simbolos | Medio-Chico (ya tiene base) |

---

*Documento generado por auditoria de codigo + comparacion con documentacion oficial de QGIS 3.44, ArcMap 10.8, ArcGIS Pro 3.x, y gvSIG 2.6.*
