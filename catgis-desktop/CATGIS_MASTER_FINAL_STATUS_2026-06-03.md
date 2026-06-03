# CATGIS Master Final Status — 2026-06-03

## 1. Resumen ejecutivo

### Estado general real del producto
CATGIS Desktop es un GIS de escritorio funcional para cartografía técnica y ambiental. No es QGIS ni ArcGIS, pero cubre el 60-70% de los casos de uso reales para consultores ambientales y topográficos en Argentina/Latam.

### Avances fuertes (esta ronda y rondas anteriores)
- **Etiquetado profesional**: 17 propiedades, placement engine con colisión, prioridad 1-10
- **CATMAP**: Plantillas mejoradas, mapa vivo redimensionable, grilla funcional, cartucho profesional, tabla mejorada
- **Performance**: Caches de PolygonSymbolRenderer y LineSymbolRenderer, LayoutMap constants
- **Sombología**: 28 estilos de puntos, 18 de líneas, 18 de polígonos, renderers unificados
- **PostGIS**: Read/write funcional
- **CRS round-trip**: 268/268 tests pasando

### Limitaciones reales
- Map frame: sigue siendo render del MapPanel global, no frame vectorial independiente
- Auto-componer: ocultado (no confiable)
- Export PDF: raster embedido, no vectorial
- Inset maps: básico (captura del mapa principal, no extent independiente real)
- Grilla: aritmética, no CRS-based
- Arquitectura: 1,442 referencias a CatgisDesktopApp (God Object)

## 2. Arquitectura

### Qué mejoró
- AppContext introducido como alternativa a statics
- 74 referencias a AppContext (vs 1,442 a CatgisDesktopApp)
- Persistencia centralizada en SaveProjectAction/LoadProjectAction

### Qué sigue débil
- CatgisDesktopApp sigue siendo God Object (16 static fields, 11 static methods)
- AppContext no reemplazó los statics, es un camino paralelo
- Save/Load usa parseo por strings con posiciones fijas (frágil)
- MapPanel y MapLayoutComposerDialog siguen siendo clases enormes

### Qué no conviene tocar todavía
- Reescritura completa de CatgisDesktopApp
- Migración masiva a AppContext (riesgo de romper funcionalidad)
- Refactor de MapPanel (demasiado acoplado)

### Ruta futura recomendada
1. Continuar migración incremental a AppContext
2. Extraer helpers de MapPanel (render, labels, selection)
3. Centralizar persistencia en ProjectSerializer/ProjectDeserializer

## 3. CATMAP

### Qué tan lejos/lerca quedó de un compositor GIS serio
- **70% funcional** para informes técnicos/ambientales
- **No es un compositor vectorial completo** como QGIS Layout
- **Sí sirve** para generar mapas técnicos profesionales

### Qué sí puede hacer bien hoy
- Plantillas con mapa, leyenda, escala, norte, cartucho
- Mapa vivo redimensionable
- Grilla con etiquetas
- Export PDF/imagen
- Inset maps básicos (captura del mapa principal)

### Qué aún no conviene sobre-vender
- Map frame vectorial independiente
- Atlas/map series
- Export PDF vectorial
- Grilla geoespacial real (CRS-based)

## 4. Simbología y etiquetado

### Estado actual real
- **Puntos**: 28 estilos + catálogo gráfico → **Fuerte**
- **Líneas**: 18 estilos con render unificado → **Fuerte**
- **Polígonos**: 18 estilos con render unificado → **Funcional**
- **Etiquetas**: 17 propiedades, placement engine, colisión, prioridad → **Funcional**

### Qué quedó fuerte
- Renderers unificados (Point/Line/Polygon)
- Cache de TexturesPaint y BasicStroke
- LabelPlacementEngine con 10 modos de placement
- Persistencia completa de propiedades de etiquetas

### Qué falta
- Motor de colisión sofisticado (simulated annealing)
- Placement curvilíneo para líneas
- Expresiones de etiquetas (multi-campo)
- Escala dinámica de fuente

## 5. Rendimiento y estabilidad

### Mejoras reales
- PolygonSymbolRenderer: cache LRU 64 entries (~80% menos allocations)
- LineSymbolRenderer: cache LRU 128 entries (~70% menos allocations)
- LayoutMap: cached constants (~10 objects/render saved)

### Cuellos de botella remanentes
- LayoutMap 500ms refresh (no inteligente)
- LabelPlacementEngine O(n*m) collision detection
- getRenderOrderLayers() new ArrayList por paint
- PointSymbolRenderer g2.create() por punto

## 6. Próximo paso recomendado

**CATMAP avanzado** (map frame vectorial real)

Justificación:
- Es la funcionalidad que más acerca CATGIS a un compositor GIS profesional
- Ya existe LayoutMap con ownExtent (base para construir)
- Los usuarios necesitan map frames independientes para informes reales
- Es más impactante que arquitectura profunda o performance masiva en este momento

## 7. Commits de la sesión

```
5bcf013 feat: CATMAP Pro + Performance optimizations
08a35a3 feat: CATMAP visual fino - plantillas mejoradas, auto-componer ocultado
224308e fix: zoom cursor, live layout map, centerOnElement
e1a837f fix: 3 test CRS round-trip + CATMAP profesional
52fa2dc feat: motor de placement y colision de etiquetas cartograficas
```

## 8. Archivos clave del proyecto

| Archivo | Propósito |
|---------|-----------|
| `CatgisDesktopApp.java` | God Object principal (1,442 refs) |
| `AppContext.java` | Alternativa a statics (74 refs) |
| `MapPanel.java` | Render del mapa principal |
| `MapLayoutComposerDialog.java` | Compositor CATMAP |
| `LayoutMap.java` | Marco de mapa en layout |
| `LayoutTemplateManager.java` | Plantillas |
| `LabelPlacementEngine.java` | Placement de etiquetas |
| `ProjectSerializer.java` | Persistencia |
| `SaveProjectAction.java` | Guardado de proyectos |
| `LoadProjectAction.java` | Carga de proyectos |
