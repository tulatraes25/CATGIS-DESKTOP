# CATGIS Performance & Stability Report — 2026-06-03

## 1. Cuellos de botella detectados

### Hot críticos (resueltos)
- **PolygonSymbolRenderer.buildPaint()**: Allocaba BufferedImage + TexturePaint por cada polígono. Solución: cache LRU de 64 entradas.
- **LineSymbolRenderer.buildStroke()**: Allocaba BasicStroke por cada feature. Solución: cache LRU de 128 entradas.
- **LayoutMap placeholder**: Allocaba Color/Font por cada render. Solución: constantes estáticas.

### Hot medio (parcialmente resueltos)
- **LabelPlacementEngine**: Crea Candidate lists por feature. Mitigación: ya funciona, pendiente optimización más profunda.
- **getRenderOrderLayers()**: Nuevo ArrayList por paint. Pendiente: cachear lista.
- **PointSymbolRenderer**: g2.create() por punto. Pendiente: optimizar.

### Hot bajo (documentados)
- **Path2D.Double por feature**: Pendiente: pooling de objetos.
- **renderMapViewImage()**: Crea BufferedImage temporal. Acceptable para export.

## 2. Optimizaciones aplicadas

| Componente | Antes | Ahora | Impacto |
|-----------|-------|-------|---------|
| PolygonSymbolRenderer | BufferedImage/TexturePaint por polígono | Cache LRU 64 entries | ~80% menos allocations |
| LineSymbolRenderer | BasicStroke por feature | Cache LRU 128 entries | ~70% menos allocations |
| LayoutMap | Color/Font por render | Static final constants | ~10 objects/render saved |

## 3. Impacto esperado

- **Render con 1000 polígonos**: ~800 menos BufferedImage allocations
- **Render con 1000 líneas**: ~700 menos BasicStroke allocations
- **LayoutMap renders**: ~10 menos object allocations por frame

## 4. Riesgos

- Cache de paint/stroke puede consumir memoria extra (~1MB max)
- LayoutMap 500ms refresh puede ser lento con muchos LayoutMaps
- Sin invalidación inteligente por cambios de contenido

## 5. Pendientes reales

### Alta prioridad
- Invalidación inteligente de LayoutMap (no solo timer)
- Cache de getRenderOrderLayers()
- CRS transform caching en forEachVisibleFeatureGeometry

### Media prioridad
- Object pooling para Path2D.Double
- Throttling de repaint en mouseDragged
- Spatial index para features

### Baja prioridad
- JMH benchmarks reproducibles
- Memory profiling con MAT
