# CATMAP Standalone Product Rework — 2026-06-03

## 1. Qué estaba mal realmente

### Problema del planisferio
- `MapFrameViewport.resolveLayerEnvelope()` para `OnlineTileLayer` devolvía el extent mundial completo `(-20037508, 20037508)`
- Esto causaba que el viewport mostrara el mundo entero en lugar del área local

### Problema del mapa vacío
- `MapFrameRenderer.render()` solo manejaba capas vectoriales
- Cuando había capas online/raster, retornaba null
- El fallback `captureMapImage()` no calculaba bien el zoom para el tamaño del frame

### Problema del standalone
- `CartographyToolbar.launchCatmapStandalone()` no encontraba java en la instalación
- CATMAP no tenía menú/toolbar/paneles propios

## 2. Qué se corrigió

### Viewport/Extent
- `MapFrameViewport.resolveLayerEnvelope()`: Para OnlineTileLayer, usa la vista actual del MapPanel en vez del extent mundial
- Si no hay vista disponible, retorna null (no world extent)

### MapFrameRenderer
- Detecta capas online/raster y retorna null para forzar fallback a MapPanel
- MapPanel maneja todos los tipos de capa correctamente

### LayoutMap.captureMapImage()
- Calcula zoom correcto para ajustar la vista del MapPanel al tamaño del frame
- Usa `fitScale = Math.min(scaleX, scaleY)` para mantener aspect ratio

### CATMAP Standalone
- `CartographyToolbar`: Busca java en múltiples paths (bin, runtime/bin, PATH)
- LayoutMap: Crea placeholder cuando ambos renderers fallan

## 3. Arquitectura de CATMAP Standalone

### Estado actual
- Ventana independiente con menú y toolbar
- Panel izquierdo de elementos
- Panel central de layout
- Panel derecho de propiedades
- Panel de capas del mapa
- Export PDF/imagen funcional

### Pendiente
- Menú completo (Archivo, Edición, Vista, Insertar, etc.)
- Toolbar con todos los iconos
- Persistencia .catmap
- Comunicación socket con CATGIS

## 4. Validación

| Comando | Resultado |
|---------|-----------|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 268/268 PASSED |
