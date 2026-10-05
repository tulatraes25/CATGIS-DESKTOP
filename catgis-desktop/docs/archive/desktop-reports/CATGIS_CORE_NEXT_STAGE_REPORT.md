# CATGIS Core Next Stage Report — 2026-06-03

## 1. Qué áreas del GIS principal se mejoraron

### Panel de capas (FASE A)
- **Botones de acción rápida**: Zoom, Propiedades, Quitar — accesibles desde la barra superior
- **Mejor UX**: Botones más grandes (32x28 en vez de 22x20), tooltips claros
- **Funciones ya existentes preservadas**: Drag & drop, menú contextual completo, visibilidad

### Tabla de atributos (FASE B)
- **Botones más grandes**: De 22x20 a 32x28 — más fáciles de hacer clic
- **Ya existente**: Field calculator, búsqueda, selección sincronizada con mapa, edición

### Selección y navegación (FASE C)
- **Ya funcional**: Selección de entidades, zoom a selección, pan/zoom, box zoom
- **Cursor**: Ya corregido en rondas anteriores (CROSSHAIR para zoom, HAND para pan)

### Simbología/labeling (FASE D)
- **Ya fuerte**: 28 estilos puntos, 18 líneas, 18 polígonos, 17 propiedades de etiquetas
- **Placement engine**: 10 modos, colisión básica, prioridad 1-10
- **Acceso**: Propiedades de capa con pestañas General/Simbología/Etiquetas

## 2. Qué estaba flojo antes

| Área | Problema | Estado |
|------|----------|--------|
| Panel de capas | Sin botones de acción rápida | **Corregido** |
| Tabla atributos | Botones demasiado pequeños (22x20) | **Corregido** |
| Toolbar principal | Sin labels en iconos | Pendiente |
| Menús | Duplicación entre Herramientas/Ver/Edicion | Pendiente |
| Drawing tools | Solo en menú, no en toolbar | Pendiente |
| Undo/Redo en atributos | No existe | Pendiente |
| Export CSV/Excel | No existe en tabla | Pendiente |

## 3. Qué se corrigió realmente

- Botones de acción rápida en panel de capas (Zoom, Propiedades, Quitar)
- Botones de tabla de atributos más grandes
- Botones de panel de capas más grandes y con labels

## 4. Qué sigue pendiente

### Alta prioridad
- Labels en toolbar principal (descubribilidad)
- Botones de drawing/measurement en toolbar
- Eliminar duplicación de menús
- Undo/Redo en atributos

### Media prioridad
- Export CSV/Excel desde tabla
- Recent files menu
- First-run tutorial
- Keyboard shortcuts en tabla

### Baja prioridad
- Column reorder/hide en tabla
- Conditional formatting
- Plugin system

## 5. Qué ya está al nivel de uso serio

- Carga de capas (SHP, GeoJSON, GeoPackage, PostGIS, WMS, WFS, GPX, KML, DXF)
- Navegación del mapa (pan, zoom, zoom a capa)
- Simbología (28+18+18 estilos, categorías)
- Etiquetado (17 propiedades, placement, colisión)
- Persistencia de proyecto
- CATMAP (compositor profesional)
- CRS management
- Edición vectorial básica

## 6. Qué todavía no conviene sobre-vender

- Motor de colisión de etiquetas sofisticado
- Atlas/map series
- PDF vectorial
- Plugin system
- Python scripting
- Análisis espacial avanzado
