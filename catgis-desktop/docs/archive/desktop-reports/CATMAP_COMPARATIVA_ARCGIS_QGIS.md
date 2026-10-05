# Comparativa Cartográfica: ArcGIS Pro vs QGIS vs CATMAP

## Elementos del Layout

| Elemento | ArcGIS Pro | QGIS | CATMAP | Prioridad |
|----------|-----------|------|--------|-----------|
| Map frame vivo | ✅ | ✅ | ✅ | — |
| Leyenda auto | ✅ | ✅ | ✅ | — |
| Escala gráfica | ✅ | ✅ | ✅ | — |
| Norte | ✅ | ✅ | ✅ | — |
| Texto libre | ✅ | ✅ | ✅ | — |
| Imagen | ✅ | ✅ | ✅ | — |
| Rectángulo/Elipse | ✅ | ✅ | ✅ | — |
| Línea | ✅ | ✅ | ✅ | — |
| Tabla de atributos | ✅ | ✅ | 🟡 Simple | — |
| Grilla CRS | ✅ | ✅ | ✅ | — |
| Cartouche | ✅ | ✅ | ✅ | — |
| **Extent indicator** | ✅ | ✅ | 🟡 Infraestructura existe | 🔴 ALTA |
| **Texto dinámico** | ✅ | ✅ | ❌ | 🔴 ALTA |
| **Grouping** | ✅ | ✅ | ❌ | 🔴 ALTA |
| **Snapping/Guías** | ✅ | ✅ | ❌ | 🔴 ALTA |
| **Bordes/sombras** | ✅ | ✅ | ❌ | 🔴 ALTA |
| **Flechas libres** | ✅ | ✅ | ❌ | 🟡 MEDIA |
| **Multi-página UI** | ✅ | ✅ | 🟡 Modelo existe | 🟡 MEDIA |
| **HTML frames** | ❌ | ✅ | ❌ | 🟢 BAJA |
| **Atlas UI** | ✅ | ✅ | 🟡 Engine existe | 🟢 BAJA |

## Herramientas de edición

| Herramienta | ArcGIS Pro | QGIS | CATMAP | Prioridad |
|------------|-----------|------|--------|-----------|
| Alinear/Distribuir | ✅ | ✅ | ✅ | — |
| Orden Z (subir/bajar) | ✅ | ✅ | ✅ | — |
| Duplicar | ✅ | ✅ | ✅ | — |
| Rotación | ✅ | ✅ | ❌ | 🟡 MEDIA |

## Exportación

| Formato | ArcGIS Pro | QGIS | CATMAP | Prioridad |
|---------|-----------|------|--------|-----------|
| PDF | ✅ | ✅ | ✅ | — |
| PNG/JPG | ✅ | ✅ | ✅ | — |
| SVG | ✅ | ✅ | ✅ | — |
| Impresión | ✅ | ✅ | ✅ | — |

## Lo que voy a implementar (prioridad ALTA)

Basado en la comparativa, lo que le falta a CATMAP para estar a la par:

### 1. Extent Indicators (ya hay infraestructura)
`MapFrameRenderer` ya tiene `setIndicatorExtent()` y `renderIndicatorRectangle()`. Solo falta:
- Agregar toggle "Mostrar extent indicator" en propiedades del LayoutMap
- Que cuando un LayoutMap es "inset", dibuje un rectángulo rojo semi-transparente mostrando dónde está el mapa principal

### 2. Texto Dinámico
Insertar texto que se actualiza automáticamente con:
- `{date}` → fecha actual
- `{scale}` → escala 1:N del mapa
- `{crs}` → CRS del proyecto
- `{project}` → nombre del proyecto
- `{time}` → hora actual
- `{page}` → número de página

### 3. Grouping
Poder agrupar elementos (Ctrl+G) y moverlos/redimensionarlos como una unidad.
Desagrupar (Ctrl+Shift+G).

### 4. Snapping / Guías
- Grilla visible con snapping cuando se arrastran elementos
- Guías arrastrables desde las reglas

### 5. Bordes y sombras en elementos
Propiedades comunes en todos los elementos:
- Borde: color, grosor, estilo
- Fondo: color, transparencia
- Sombra: offset, blur, color
