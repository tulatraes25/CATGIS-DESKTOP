# CATMAP Fix Plan — Fases

## Diagnóstico

### Problema 1: Viewport / planisferio
El `MapFrameViewport.fitFromMainMap()` toma el envelope del `MapPanel` aunque esté vacío (standalone crea un `MapPanel` nuevo con view `viewMinX=0, viewMinY=0, zoomFactor=1` que da un envelope de ~800×600 unidades alrededor de (0,0)). El proyecto real está en Argentina (~-68,-33) así que el viewport no muestra los datos—se ve en blanco. `fitFromMainMap()` devuelve `true` aunque el viewport esté mal, entonces el fallback a `fitFromProjectLayers()` nunca se ejecuta.

### Problema 2: Dual-rendering en MapLayoutComposerDialog
`LayoutRenderer.render()` dibuja el mapa desde un snapshot estático. `renderLayout()` luego overlayea elementos del `layoutModel` incluyendo un `LayoutMap`. Hay DOS mapas compitiendo: el snapshot y el LayoutMap.

### Problema 3: Standalone
El standalone ya tiene estructura completa de menús, toolbar, paneles y export. No necesita reescribirse, solo pulirse visualmente y arreglar el viewport.

## Fases

### FASE 1 (crítico) — Reparación del viewport
- [x] `MapFrameViewport.fitFromMainMap()`: si el MapPanel tiene vista vacía que no intersecta con las capas, usar extent de capas visibles
- [ ] `LayoutMap.syncViewportToSource()`: priorizar capas visibles cuando standalone
- [ ] `MapFrameRenderer.render()`: manejar correctamente raster/online layers (hoy retorna null y delega a MapPanel, que en standalone es un dummy)

### FASE 2 — Eliminar dual-rendering en CATGIS
- [ ] `renderLayout()` en `MapLayoutComposerDialog`: si hay `LayoutMap` en el modelo, no dibujar el map frame desde el snapshot

### FASE 3 — Standalone pulido
- [ ] Mejorar visual: icons, layout, paneles
- [ ] Fix `LayoutExportEngine` orientation (ya hecho)
- [ ] Mejorar connection state

### FASE 4 — Documentación
- [ ] `CATMAP_STANDALONE_PRODUCT_REWORK.md`
- [ ] `CATMAP_STANDALONE_UX_REPORT.md`

### FASE 5 — Build & Validación
- `gradlew compileJava`
- `gradlew test`
- `gradlew build -x checkstyleMain -x checkstyleTest`
