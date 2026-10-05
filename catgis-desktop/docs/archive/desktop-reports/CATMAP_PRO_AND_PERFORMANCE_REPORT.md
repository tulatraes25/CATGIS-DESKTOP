# CATMAP Pro and Performance Report — 2026-06-03

## 1. Resumen ejecutivo

### Qué se atacó
- Ronda combinada: CATMAP profesional + Performance/Estabilidad
- 14 bloques de trabajo (A1-A7 CATMAP, B1-B5 Performance, C Consistencia, D Documentación)

### Qué mejoró de verdad

**Performance (medible):**
- `PolygonSymbolRenderer`: Cache de TexturePaint por estilo/color/tamaño. Antes: BufferedImage nueva por cada polígono. Ahora: reutiliza hasta 64 entradas.
- `LineSymbolRenderer`: Cache de BasicStroke por estilo/ancho. Antes: BasicStroke nueva por cada feature. Ahora: reutiliza hasta 128 entradas.
- `LayoutMap`: Constantes estáticas para placeholder y font de grilla. Antes: new Color/Font por cada render. Ahora: static final.

**CATMAP:**
- Auto-componer ocultado del toolbar (no era confiable)
- 8 plantillas A4 mejoradas (mejores proporciones, colores diferenciados, subtítulos)
- 2 plantillas A3 mejoradas
- Grilla cartográfica funcional (distance-based, con etiquetas)
- Cartucho profesional (título, fondo semi-transparente, mejor spacing)
- Tabla con header oscuro y separadores de fila
- Zoom cursor corregido (CROSSHAIR para zoom, HAND para pan)
- Mapa vivo con cache refresh cada 500ms
- centerOnElement funcional (scroll real al viewport)

### Qué quedó pendiente
- Map frame vectorial real (no render del MapPanel global)
- Inset maps / mapa de ubicación (no implementado en esta ronda)
- Atlas/map series
- Export PDF vectorial (texto y líneas vectoriales)
- Motor de colisión de etiquetas más sofisticado
- Grilla geoespacial real (CRS-based)

## 2. CATMAP profesional

### Grilla (LayoutMap.java)
- Distance-based grid con intervalos configurables
- Etiquetas de coordenadas en bordes
- Grosor configurable
- Control de visibilidad de etiquetas
- Fallback a subdivisión simple

### Marco de mapa
- Borde configurable (color, grosor, radio de esquina)
- Consistencia entre preview y export
- Resize funcional con 8 handles

### Inset maps
- **No implementado en esta ronda** (documentado como pendiente)
- Requiere: LayoutMap secundario con ownExtent, rectángulo indicador

### Tablas (LayoutTable.java)
- Header oscuro (#2D3748) con texto blanco
- Separadores de fila sutiles
- Filas alternadas
- Bordes y padding mejorados

### Cartucho (LayoutCartouche.java)
- Barra de título "Datos Cartográficos"
- Fondo semitransparente
- Tipografía reducida (7-9pt)
- Bordes redondeados
- Auto-ajuste de alto

### Exportación
- PDF: raster embedido en PDFBox (funcional, no vectorial)
- PNG/JPG: DPI configurable
- Impresión directa funcional

### Plantillas retocadas

| Plantilla | Color | Mapa | Leyenda |
|-----------|-------|------|---------|
| A4_TECNICO | #1A2434 | 190x138 | Derecha con fondo |
| A4_AMBIENTAL | #1B5E20 | 273x132 | Inferior |
| A4_CATASTRAL | #BF360C | 195x138 | Derecha con fondo |
| A4_HIDROLOGIA | #0D47A1 | 273x132 | Inferior |
| A4_TOPOGRAFIA | #33691E | 273x132 | Inferior |
| A4_INFRAESTRUCTURA | #4A148C | 273x132 | Inferior |
| A3_TECNICO | #1A2434 | 280x228 | Derecha con fondo |
| A3_AMBIENTAL | #1B5E20 | 390x220 | Inferior |

## 3. Performance y estabilidad

### Cuellos de botella detectados

| Problema | Ubicación | Severidad |
|----------|-----------|-----------|
| LayoutMap re-render cada 500ms (incluso sin cambios) | LayoutMap.java:78 | Alta |
| PolygonSymbolRenderer: BufferedImage por polígono | PolygonSymbolRenderer.java:15 | Alta |
| LineSymbolRenderer: BasicStroke por feature | LineSymbolRenderer.java:47 | Media |
| PointSymbolRenderer: g2.create() por punto | PointSymbolRenderer.java:27 | Media |
| LabelPlacementEngine: Candidate lists por feature | LabelPlacementEngine.java:109 | Media |
| getRenderOrderLayers(): nuevo ArrayList por paint | MapPanel.java:6106 | Media |
| Path2D.Double por feature | MapPanel.java:7558 | Baja |
| Color/Font constantes recreados por render | LayoutMap.java:99 | Baja |

### Optimizaciones aplicadas

| Optimización | Impacto esperado |
|-------------|-----------------|
| PolygonSymbolRenderer cache (64 entries) | Reduce allocations ~80% para polígonos |
| LineSymbolRenderer cache (128 entries) | Reduce allocations ~70% para líneas |
| LayoutMap cached constants | Evita ~10 new objects por render |
| Auto-componer ocultado | Elimina renders innecesarios |

### Riesgos
- Cache de paint/stroke puede consumir memoria si hay muchos estilos diferentes (limitado a 64/128 entries)
- LayoutMap 500ms refresh puede ser lento con muchos LayoutMaps (pendiente: invalidación más inteligente)

## 4. Validación ejecutada

| Comando | Resultado |
|---------|-----------|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 268/268 PASSED |
| `gradlew build` | BUILD SUCCESSFUL |

### Escenarios de prueba
- Mapa con 1000+ features: placement funciona, render aceptable
- Layout con leyenda + tabla + mapa vivo: funcional
- Export PDF: funcional
- Export imagen: funcional

## 5. Limitaciones honestas

### Sigue parcial
- Map frame: sigue siendo render del MapPanel global, no frame vectorial independiente
- Grilla: aritmética, no CRS-based
- Export PDF: raster embedido, no vectorial
- Inset maps: no implementado
- Auto-componer: ocultado, no resuelto

### No debería venderse como final
- "CATMAP es un compositor GIS profesional" → Es funcional para informes técnicos, no al nivel de QGIS Layout
- "Performance optimizada" → Se mejoraron hotspots específicos, no es una optimización profunda
- "Plantillas profesionales" → Son funcionales y mejoradas, pero no al nivel de plantillas curadas de ArcGIS

## 6. Próximo paso recomendado

**Seguir con CATMAP fino:**
1. Inset maps / mapa de ubicación
2. Map frame vectorial real
3. Export PDF vectorial

**Justificación:** Estas son las funcionalidades que más acercan CATGIS a un compositor GIS real para informes técnicos. La performance ya es aceptable para el uso típico.

## 7. Archivos tocados

| Archivo | Cambio |
|---------|--------|
| `PolygonSymbolRenderer.java` | Cache de TexturePaint |
| `LineSymbolRenderer.java` | Cache de BasicStroke |
| `LayoutMap.java` | Cached constants, grid improvements |
| `MapLayoutComposerDialog.java` | Auto-componer ocultado, zoom cursor fix |
| `LayoutTemplateManager.java` | 10 plantillas mejoradas |
| `CATMAP_PRO_VISUAL_FINAL_REPORT.md` | Documentación previa |
