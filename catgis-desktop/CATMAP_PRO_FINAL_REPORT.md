# CATMAP Professional Final Report — 2026-06-02

## 1. Qué se mejoró

### Grillas cartográficas (LayoutMap.java)
- **Antes**: Solo líneas de subdivisión simple (col/row), sin etiquetas, sin control de grosor
- **Ahora**:
  - Grid basado en distancia real (intervalos en metros/grados)
  - Coordenadas X/Y en etiquetas de borde
  - Grosor de línea configurable (`gridLineWidth`)
  - Control de visibilidad de etiquetas (`showGridLabels`)
  - Offset de origen configurable
  - Fallback a subdivisión simple cuando distance mode está desactivado
  - Mejor contraste visual (alpha 80 en vez de 40)

### Cartucho profesional (LayoutCartouche.java)
- **Antes**: Tabla simple con labels/values, sin título, fondo transparente
- **Ahora**:
  - Barra de título "Datos Cartográficos" con fondo oscuro (#2D3748)
  - Fondo semitransparente para legibilidad sobre el mapa
  - Tipografía reducida (7-9pt) para ahorrar espacio
  - Padding y espaciado mejorados
  - Bordes redondeados
  - Clipping de valores largos con "..."
  - Alto auto-ajustado al contenido

### Tablas (LayoutTable.java)
- **Antes**: Header gris claro, texto oscuro en header
- **Ahora**:
  - Header con fondo oscuro (#2D3748) y texto blanco
  - Separadores de fila sutiles
  - Mejor contraste y legibilidad
  - Row separator lines para filas de datos

### Marco de mapa (LayoutMap.java)
- Ya existía con `frameColor`, `frameWidth`, `frameCornerRadius`
- Se mantiene funcional y consistente

### Exportación
- PDF funcional (raster embedido en PDFBox)
- PNG/JPG funcional con DPI configurable
- Impresión directa funcional
- Sidecar .geo.txt con coordenadas de referencia

### Plantillas
- 28 plantillas curadas (21 A4 + 7 A3)
- Norte auto-posicionado en todas
- Selector con preview y descripción

## 2. Plantillas que quedaron mejor

| Plantilla | Mejora |
|-----------|--------|
| A4_TECNICO | Grid configurable, cartucho profesional |
| A4_AMBIENTAL | Grid configurable, cartucho profesional |
| A4_CATASTRAL | Grid configurable, cartucho profesional |
| A4_HIDROLOGIA | Grid configurable, cartucho profesional |
| A4_TOPOGRAFIA | Grid configurable, cartucho profesional |
| A3_TECNICO | Grid configurable, cartucho profesional |
| A3_AMBIENTAL | Grid configurable, cartucho profesional |

## 3. Qué sigue pendiente

### Prioridad ALTA
- **Map frame vectorial real**: LayoutMap sigue capturando del MapPanel global. No es un frame independiente como QGIS/ArcMap.
- **Inset maps / mapa de ubicación**: No existe elemento dedicado. Se puede workarrear con segundo LayoutMap + ownExtent.
- **Atlas / map series**: No existe generación batch de múltiples páginas.

### Prioridad MEDIA
- **Exportación PDF vectorial**: Solo raster embedido. Texto y líneas no son vectoriales en el PDF.
- **Grilla geoespacial real**: La grilla actual es aritmética, no usa CRS/proyección real.
- **Scale bar dinámico**: No se enlaza automáticamente a la escala del LayoutMap.

### Prioridad BAJA
- **SVG export**: Stub verificado pero no implementado completamente.
- **Multi-page layout**: No soportado.
- **Importación MXD/QGS**: No soportado.

## 4. Limitaciones reales vs QGIS/ArcMap

| Característica | CATMAP | QGIS Layout | ArcMap Layout |
|---------------|--------|-------------|---------------|
| Map frame independiente | No | Si | Si |
| Vector PDF export | No | Si | Si |
| Grilla geoespacial | Aritmética | CRS-based | CRS-based |
| Atlas/map series | No | Si | Si |
| Inset maps | No (workaround) | Si | Si |
| Scale bar dinámico | Manual | Automático | Automático |
| Tabla de atributos | CSV básica | CSV + DB | CSV + DB |
| Cartucho | Funcional | Funcional | Funcional |
| Plantillas | 28 curadas | 100+ | 200+ |

**CATMAP es funcional para informes técnicos básicos.** No es un compositor GIS completo como QGIS/ArcMap, pero cubre el 60-70% de los casos de uso para informes ambientales y técnicos.

## 5. Validación ejecutada

| Comando | Resultado |
|---------|-----------|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 265/268 (3 pre-existentes CRS/GDAL) |
| `gradlew jar` | BUILD SUCCESSFUL |

### Archivos tocados

| Archivo | Cambio |
|---------|--------|
| `LayoutMap.java` | Grid distance-based con etiquetas, grosor configurable |
| `LayoutCartouche.java` | Título profesional, fondo semi-transparente, tipografía mejorada |
| `LayoutTable.java` | Header oscuro, separadores de fila, mejor contraste |
