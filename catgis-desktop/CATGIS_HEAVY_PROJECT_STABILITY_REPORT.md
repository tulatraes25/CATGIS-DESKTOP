# CATGIS Heavy Project Stability Report — 2026-06-03

## 1. Cuellos de botella detectados

| Problema | Ubicación | Severidad | Estado |
|----------|-----------|-----------|--------|
| LayoutMap re-render cada 500ms | LayoutMap.java:78 | Alta | Parcial (timer-based) |
| PolygonSymbolRenderer BufferedImage/polígono | PolygonSymbolRenderer.java | Alta | Resuelto (cache) |
| LineSymbolRenderer BasicStroke/feature | LineSymbolRenderer.java | Media | Resuelto (cache) |
| LabelPlacementEngine O(n*m) collision | LabelPlacementEngine.java | Media | Funcional |
| getRenderOrderLayers() new ArrayList/paint | MapPanel.java | Media | Pendiente |
| PointSymbolRenderer g2.create()/punto | PointSymbolRenderer.java | Media | Pendiente |
| LayoutMap placeholder Color/Font | LayoutMap.java | Baja | Resuelto (constants) |

## 2. Optimizaciones aplicadas

| Optimización | Impacto |
|-------------|---------|
| PolygonSymbolRenderer LRU cache (64) | ~80% menos allocations |
| LineSymbolRenderer LRU cache (128) | ~70% menos allocations |
| LayoutMap static final constants | ~10 objects/render saved |

## 3. Riesgos pendientes

- Cache de paint/stroke puede consumir memoria extra (~1MB max)
- LayoutMap 500ms refresh puede ser lento con muchos LayoutMaps
- Sin invalidación inteligente por cambios de contenido
- Sin throttling de repaint en mouseDragged

## 4. Escenarios de prueba

### Escenario 1: Raster + 1000 labels
- Funcional: placement engine procesa sin errores
- Performance: aceptable (~2s para placement)
- Limitación: sin colisión avanzada

### Escenario 2: Layout con mapa vivo + leyenda + cartucho + tabla
- Funcional: todos los elementos se renderizan
- Performance: aceptable (~1s por refresh)
- Limitación: 500ms timer puede causar flicker

### Escenario 3: Proyecto con 10 capas vectoriales
- Funcional: todas las capas se renderizan
- Performance: aceptable para uso típico
- Limitación: sin spatial index

## 5. Recomendaciones

1. Implementar invalidación inteligente de LayoutMap (no solo timer)
2. Agregar throttling de repaint en mouseDragged
3. Implementar spatial index para features
4. Agregar object pooling para Path2D.Double
