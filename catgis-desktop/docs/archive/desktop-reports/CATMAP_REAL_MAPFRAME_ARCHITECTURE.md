# CATMAP Real Map Frame Architecture — 2026-06-03

## 1. Arquitectura anterior

```
LayoutMap → CatgisDesktopApp.mapPanel → MapPanel.renderMapViewImage()
                                        ↓
                                   paintComponent(g2)
                                        ↓
                              drawLayer() / drawAllLabels()
                                        ↓
                                   BufferedImage
```

**Problema**: Todo dependía del MapPanel global. Múltiples LayoutMaps mostraban la misma vista. No había independencia real.

## 2. Arquitectura nueva

```
LayoutMap → MapFrameRenderer → Project.getLayers()
                ↓                    ↓
        MapFrameViewport      renderLayer() / renderLabels()
                ↓                    ↓
        extent/scale/center    BufferedImage independiente
```

**Solución**: Cada LayoutMap tiene su propio MapFrameRenderer y MapFrameViewport. Renderiza directamente desde las capas del proyecto.

## 3. Componentes creados

| Componente | Propósito |
|-----------|-----------|
| `MapFrameRenderer` | Renderiza capas del proyecto directamente en un BufferedImage |
| `MapFrameViewport` | Estado de viewport independiente (extent, scale, center) |
| `LayoutMap.getViewport()` | Acceso al viewport del map frame |
| `LayoutMap.getIndependentRenderer()` | Acceso al renderer independiente |
| `LayoutMap.invalidateRenderCache()` | Invalidación manual del cache |

## 4. Flujo de render del map frame

1. `LayoutMap.render()` es llamado por el compositor
2. Intenta renderizar con `renderIndependent()` (nuevo renderer)
3. Si falla, cae a `captureMapImage()` (legacy MapPanel)
4. El renderer independiente:
   - Obtiene capas del proyecto
   - Para cada capa visible, itera features
   - Renderiza puntos/líneas/polígonos con renderers unificados
   - Renderiza etiquetas con LabelPlacementEngine
   - Retorna BufferedImage

## 5. Relación con simbología, labels y export

- **Simbología**: Usa PointSymbolRenderer, LineSymbolRenderer, PolygonSymbolRenderer directamente
- **Labels**: Usa LabelPlacementEngine con collision detection
- **Export**: El mismo renderer se usa para preview y export (consistencia)
- **Grilla**: Se dibuja encima del contenido renderizado

## 6. Estado actual

- **Funcional**: LayoutMap puede renderizar independientemente
- **Backward compatible**: Si el renderer independiente falla, cae a MapPanel
- **Múltiples mapas**: Soportado (cada uno con su viewport)
- **Inset maps**: Posible con ownExtent + viewport independiente

## 7. Pendiente

- Integración completa con pan/zoom del compositor
- Rectángulo indicador de área en inset
- Escala dinámica del LayoutScaleBar
- Leyenda por frame (no por proyecto)
