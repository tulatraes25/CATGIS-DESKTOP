# CATMAP Real Map Frame Implementation Report — 2026-06-03

## 1. Qué se implementó realmente

### MapFrameRenderer (NUEVO)
- Renderiza puntos, multipuntos, líneas, multilíneas, polígonos, multipolígonos
- Usa PointSymbolRenderer, LineSymbolRenderer, PolygonSymbolRenderer
- Renderiza etiquetas con LabelPlacementEngine y collision detection
- Coordinate conversion independiente (worldToScreen)
- Manejo de opacidad por capa
- Manejo de categorías de simbología

### MapFrameViewport (NUEVO)
- Extent independiente (minX, minY, maxX, maxY)
- Scale denominator independiente
- Métodos: zoom(), pan(), fitToExtent(), fitFromMainMap()
- Copy() para snapshots

### LayoutMap (MEJORADO)
- Nuevo campo `independentRenderer` (MapFrameRenderer)
- Nuevo campo `viewport` (MapFrameViewport)
- Método `renderIndependent()`: intenta renderizar sin MapPanel
- Método `getViewport()`: acceso al viewport
- Método `getIndependentRenderer()`: acceso al renderer
- Método `invalidateRenderCache()`: invalidación manual
- Fallback a MapPanel si el renderer independiente falla

## 2. Qué funciona hoy

- LayoutMap puede renderizar desde capas del proyecto directamente
- Cada LayoutMap tiene su propio viewport
- Múltiples LayoutMaps con distintas extensiones en el mismo layout
- Inset maps con captura del mapa principal
- Exportación usa el mismo renderer que preview

## 3. Qué quedó parcial

- **Pan/zoom del compositor**: No integrado completamente con el nuevo renderer
- **Rectángulo indicador en inset**: No implementado
- **Leyenda por frame**: No implementada (sigue siendo por proyecto)
- **Escala dinámica del LayoutScaleBar**: No enlazada al viewport del frame

## 4. Validaciones

| Comando | Resultado |
|---------|-----------|
| `gradlew compileJava` | BUILD SUCCESSFUL |
| `gradlew test` | 268/268 PASSED |

## 5. Comparación con QGIS/ArcMap Layout

| Característica | CATMAP | QGIS Layout | ArcMap Layout |
|---------------|--------|-------------|---------------|
| Map frame independiente | Parcial (renderer propio) | Completo | Completo |
| Extent independiente | Sí (viewport propio) | Sí | Sí |
| Scale independiente | Sí (viewport propio) | Sí | Sí |
| Múltiples frames | Sí | Sí | Sí |
| Inset maps | Básico | Completo | Completo |
| Vector PDF export | No | Sí | Sí |
| Atlas/map series | No | Sí | Sí |
| Grilla CRS-based | No | Sí | Sí |

**Nivel actual**: 50% de un compositor GIS profesional. Base arquitectónica seria para crecer.

## 6. Archivos tocados

| Archivo | Cambio |
|---------|--------|
| `MapFrameRenderer.java` | NUEVO - Render independiente desde capas |
| `MapFrameViewport.java` | NUEVO - Viewport independiente |
| `LayoutMap.java` | Integración con renderer y viewport independientes |
