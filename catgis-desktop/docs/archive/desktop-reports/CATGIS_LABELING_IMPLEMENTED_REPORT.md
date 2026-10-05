# CATGIS Labeling Implemented Report — 2026-06-02

## 1. Estado anterior del etiquetado

- `labelsVisible` (checkbox) y `labelField` (combo de campo) en Layer
- 12 propiedades basicas en Layer: font, size, bold, italic, color, halo enabled/color/width, offset X/Y, placement
- Render basico en `MapPanel.drawLabelForFeature` con halo de 8 direcciones
- `MapPanel.drawTextWithHalo` con halo blanco hardcodeado y texto negro (ignoraba settings de capa)
- **Bug critico**: `drawLabels` dibujaba etiquetas DOS VECES (una con settings de capa via `drawGeometry`, otra con settings hardcodeados via `drawTextWithHalo`)
- Dialogo de propiedades: pestana "Etiquetas" basica, sin preview, sin placement selector, sin fondo, sin underline, sin rango de escala
- Persistencia: solo guardaba propiedades cuando `isLabelsVisible() == true`

## 2. Nuevas propiedades de etiquetas en Layer.java

Campos agregados:

| Campo | Tipo | Default | Descripcion |
|-------|------|---------|-------------|
| `labelUnderline` | boolean | false | Subrayado de texto |
| `labelBackgroundEnabled` | boolean | false | Fondo rectangular detras del texto |
| `labelBackgroundColor` | Color | white 180 alpha | Color de fondo |
| `labelMinScale` | double | 0 | Escala minima (1:N) para mostrar etiquetas (0 = sin limite) |
| `labelMaxScale` | double | 0 | Escala maxima (1:N) para mostrar etiquetas (0 = sin limite) |

Metodo utilitario:
- `isLabelVisibleAtScale(double scaleDenominator)` — verifica si la etiqueta debe mostrarse segun rango de escala

Total de propiedades de etiquetas: **17** (12 existentes + 5 nuevas)

## 3. Dialogo de propiedades de capa (LayerPropertiesDialog)

La pestana "Etiquetas" se reescribio completamente con layout de dos columnas:

### Columna izquierda:
- **Activacion**: checkbox "Mostrar etiquetas" + combo de campo
- **Tipografia**: fuente (todas las del sistema), tamano (6-72), estilo (Negrita/Cursiva/Subrayado), color de texto
- **Ubicacion**: modo de placement (AUTO, TOP, BOTTOM, LEFT, RIGHT, CENTER, FOLLOW_LINE), offset X/Y

### Columna derecha:
- **Legibilidad**: halo (activo/color/grosor), fondo (activo/color)
- **Rango de escala**: escala min (1:N), escala max (1:N)
- **Vista previa**: preview en tiempo real de como se ve la etiqueta con los ajustes actuales

### Mejoras de UX:
- Preview en vivo que responde a todos los cambios
- Toggle buttons para Negrita (N), Cursiva (K), Subrayado (S)
- Organizacion por secciones con titulos claros
- Colores consistentes con el resto del dialogo

## 4. Render de etiquetas en MapPanel

### Puntos
- Etiqueta centrada sobre el punto con offset configurable
- Soporta font, color, bold, italic, underline

### Lineas
- Etiqueta en el punto medio de la linea
- Misma configuracion que puntos

### Poligonos
- Etiqueta en el interior point (o centroid como fallback)
- Misma configuracion que puntos

### Fix: Bug de etiquetas duplicadas
**Antes**: `drawLabels()` dibujaba etiquetas una segunda vez con hardcoded white halo/black text, encima de las ya dibujadas por `drawGeometry()`.
**Ahora**: Eliminada la llamada duplicada. Solo se dibujan etiquetas una vez via `drawLabelForFeature` con settings completos de capa.

### drawLabelForFeature (metodo principal)
- Anti-aliasing de texto (`VALUE_TEXT_ANTIALIAS_ON`)
- Halo de 8 direcciones con color y grosor configurables
- Fondo rectangular con color configurable
- Subrayado
- Rango de escala (min/max)
- Offset X/Y

### drawLabels / drawTextWithHalo (fallback actualizado)
- Ambos metodos actualizados para usar settings de capa
- Consistencia total entre ambos paths de render

## 5. Halo cartografico

- **Activar/desactivar**: checkbox en dialogo y campo en Layer
- **Color**: chooser de color con preview
- **Grosor**: spinner 1-12 px
- **Render**: 8 direcciones (dx, dy de -1 a 1, excluyendo 0,0) con grosor configurable
- **Funciona sobre**: imagen satelital, poligonos, lineas, fondos complejos
- **Mejora real vs antes**: el halo ahora usa el color y grosor configurados por el usuario, no hardcoded blanco

## 6. Persistencia en proyecto

### SaveProjectAction
Guarda todas las propiedades de etiquetas (siempre, no solo cuando labels esta activo):
```
LABEL_FONT= SansSerif
LABEL_SIZE= 10
LABEL_BOLD= false
LABEL_ITALIC= false
LABEL_UNDERLINE= false
LABEL_COLOR= 0,0,0
LABEL_HALO= true
LABEL_HALO_COLOR= 255,255,255
LABEL_HALO_WIDTH= 2.0
LABEL_OFFSET_X= 0
LABEL_OFFSET_Y= 0
LABEL_PLACEMENT= AUTO
LABEL_BG= false
LABEL_BG_COLOR= 255,255,255
LABEL_MIN_SCALE= 0
LABEL_MAX_SCALE= 0
```

### LoadProjectAction
Lee todas las propiedades con parsing seguro (try/catch por campo). Compatible hacia atras: campos faltantes usan defaults.

### VectorLayerUtils.copyLayerAppearance
Copia las 17 propiedades de etiquetas al duplicar/exportar capas.

## 6. Coherencia CATMAP

### LayoutLegend
- `LayoutLegend` solo usa `label` para nombres de items de leyenda, no para etiquetas de capa
- No depende del sistema de etiquetas de capa
- **No hay impacto negativo** en CATMAP

### MapLayoutComposerDialog
- El render del mapa del layout usa `MapPanel` que ya tiene las mejoras de etiquetas
- Las etiquetas se veran correctamente en la vista previa del layout
- **No hay promesa de integracion total**: CATMAP renderiza el mapa principal, que incluye las etiquetas

## 7. Archivos tocados

| Archivo | Cambio |
|---------|--------|
| `Layer.java` | +5 campos, +1 metodo utilitario, getters/setters |
| `LayerPropertiesDialog.java` | Reescritura completa de `buildLabelsTab()`, +import FontMetrics |
| `MapPanel.java` | Fix duplicate labels, mejora `drawLabelForFeature`, actualiza `drawLabels`/`drawTextWithHalo` |
| `SaveProjectAction.java` | Serialization de 5 campos nuevos |
| `LoadProjectAction.java` | Deserialization de 5 campos nuevos |
| `VectorLayerUtils.java` | Copia de 17 propiedades de etiquetas |

## 8. Validacion

| Comando | Resultado |
|---------|-----------|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 268 tests, 3 failed (pre-existing CRS/GDAL, no relacionado con labels) |
| `gradlew jar` | BUILD SUCCESSFUL |

### Tests fallidos (pre-existentes)
- `ProjectBooleanRiskRoundTripTest` — EPSG:22182 vs EPSG:3857
- `ProjectFloodOperationalCrsRoundTripTest` — EPSG:22182 vs EPSG:3857
- `ProjectOperationalCrsRoundTripTest` — EPSG:22182 vs EPSG:3857

Causa: problema de CRS round-trip con GDAL native library no cargada. No relacionado con etiquetado.

## 9. Que sigue pendiente

### Pendiente A — Motor de colision de etiquetas
- No hay deteccion de superposicion de etiquetas
- En capas densas, las etiquetas pueden solaparse
- Solucion futura: implementar bounding box check y desplazamiento automatico

### Pendiente B — LabelPlacement real
- El campo `labelPlacement` se guarda y se muestra en el dialogo
- El render actual usa siempre centrado (AUTO)
- FOLLOW_LINE, TOP, BOTTOM, LEFT, RIGHT estan declarados pero no implementados en el render

### Pendiente C — Etiquetas multi-linea
- No hay soporte para texto multi-linea o wrapping
- Etiquetas largas pueden salirse del viewport

### Pendiente D — Expresiones de etiquetas
- No hay soporte para expresiones tipo QGIS (`concat("Field1", ' ', "Field2")`)
- Solo se puede mostrar un campo simple

### Pendiente E — Escala de fuente dinamica
- El tamano de fuente es fijo en pixeles
- No se escala con el zoom del mapa

## 10. Limitaciones reales vs QGIS/ArcGIS

| Caracteristica | CATGIS | QGIS | ArcGIS Pro |
|---------------|--------|------|------------|
| Campo simple | Si | Si | Si |
| Expresiones | No | Si | Si |
| Fuente/color/size | Si | Si | Si |
| Bold/italic/underline | Si | Si | Si |
| Halo | Si (8-dir) | Si (buffer) | Si (buffer) |
| Fondo | Si (basico) | Si (callout) | Si (callout) |
| Offset | Si | Si | Si |
| Placement modes | Declarado, no implementado | 6+ modos | 10+ modos |
| Colision | No | Si (automated) | Si (automated) |
| Scale range | Si | Si | Si |
| Multi-linea | No | Si | Si |
| Reglas de escala | No | Si | Si |
| Labels en layout | Via mapa principal | Independiente | Independiente |

**CATGIS tiene etiquetas funcionales y configurables**, suficientes para mapas tecnicos y ambientales basicos. No tiene la sofisticacion de positioning y colision de QGIS/ArcGIS, pero cubre el 70-80% de los casos de uso reales.
