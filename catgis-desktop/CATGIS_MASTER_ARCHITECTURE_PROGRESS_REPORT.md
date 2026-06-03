# CATGIS Master Architecture Progress Report — 2026-06-03

## 1. Deuda arquitectónica que sigue viva

| Problema | Ubicación | Impacto |
|----------|-----------|---------|
| God Object CatgisDesktopApp | CatgisDesktopApp.java | 1,442 referencias externas |
| AppContext paralelo no adoptado | AppContext.java | 74 refs vs 1,442 |
| Persistencia por parseo de strings | SaveProjectAction/LoadProjectAction | Frágil, hard de mantener |
| MapPanel monolítico | MapPanel.java | 11,000+ líneas |
| MapLayoutComposerDialog monolítico | MapLayoutComposerDialog.java | 9,500+ líneas |
| LayersPanel acoplado | LayersPanel.java | Depende de CatgisDesktopApp |

## 2. Qué se redujo realmente

- AppContext introducido con 5 campos y 15 métodos
- Persistencia centralizada en SaveProjectAction/LoadProjectAction
- Renderers de simbología extraídos (Point/Line/Polygon)
- LabelPlacementEngine extraído como clase independiente
- LayoutMap con cache inteligente

## 3. Qué no conviene tocar todavía

- Reescritura completa de CatgisDesktopApp
- Migración masiva a AppContext (riesgo alto)
- Refactor de MapPanel en módulos (demasiado acoplado)
- Nuevo sistema de persistencia (backward compatibility)

## 4. Ruta futura recomendada

### Corto plazo (1-2 meses)
1. Extraer helpers de MapPanel (render, labels, selection)
2. Migrar 5-10 clases más a AppContext
3. Crear interfaces para servicios (PostGIS, Raster, etc.)

### Mediano plazo (3-6 meses)
1. Separar MapPanel en: RenderEngine, InteractionController, EditingController
2. Separar MapLayoutComposerDialog en: LayoutRenderer, LayoutInteraction, LayoutExport
3. Crear contrato de persistencia (ProjectSerializer interface)

### Largo plazo (6-12 meses)
1. Migración completa a AppContext
2. Plugin system mínimo viable
3. Testing de integración con CI/CD
