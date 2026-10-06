# P0 — Pan Lag — Design

Status: ACTIVE
Baseline: `ffe6337f738a9972e24de4785a4ed0735bbac0d4`

## Selected design: PAN PREVIEW = SNAPSHOT + TRANSLATION

Do NOT move GeoTools rendering to another thread in this phase.

```
MapPanel
  +-- normal definitive rendering
  +-- PanPreviewState (interaction state/metrics only)
  +-- reusable BufferedImage panPreviewImage
```

### At pan start
1. capture the scrollable map scene once into `panPreviewImage`;
2. do NOT include screen-fixed decorations in that snapshot;
3. set cumulative preview offset = 0;
4. mark preview active.

### During active pan
1. mutate the actual viewport with the existing math;
2. accumulate pixel dx/dy;
3. repaint;
4. `paintComponent` detects active valid preview;
5. draw `panPreviewImage` translated by cumulative dx/dy;
6. render only lightweight screen-fixed decorations;
7. do NOT traverse layers or run labels/heatmap/clusters again.

### At release
1. deactivate preview;
2. update pointer/status once;
3. repaint normally;
4. definitive render uses the final viewport.

### Fallback
If snapshot capture cannot be created, preserve current rendering behavior rather than breaking navigation.

## Components

### PanPreviewState (NEW)
Package-private final class owning only interaction state/metrics: `active`, `offsetX`, `offsetY`, `startedNanos`, `snapshotNanos`, `previewPaintCount`, `maxPreviewPaintNanos`. Methods: `begin(snapshotNanos)`, `shift(dx,dy)`, `recordPreviewPaint(elapsedNanos)`, `finish()`, `reset()`. No Layer/Graphics/Swing references.

### MapPanel extraction
Extract the current paint body into:
- `renderScrollableScene(Graphics2D)` — layers, labels, heatmap, clusters, selection geometries, pins, sketches, snap preview, selection box.
- `renderFixedScreenDecorations(Graphics2D)` — screen-fixed map decoration pipeline + attribution.

Normal rendering output/order must remain equivalent.

### Pan snapshot
MapPanel owns one reusable `BufferedImage panPreviewImage`. `beginPanPreview()`: require valid width/height; create or reuse; clear with panel background; `renderScrollableScene()` ONCE; begin `PanPreviewState`. Do not include fixed decorations; do not call `paintComponent` recursively.

### Preview paint path
`paintComponent()`: `super.paintComponent(g)`; if a valid pan preview is active AND not rendering an export → draw cached scene at (offsetX,offsetY), `renderFixedScreenDecorations(g2)`, record metrics, return; otherwise normal definitive path. A preview repaint must not call `getRenderOrderLayers()`, `drawOnlineTileLayer()`, `drawOnlineWmsLayer()`, `drawRasterLayer()`, `drawLayer()`, `drawAllLabels()`, `drawHeatmapOverlay()`, `drawPointClusters()` (except during the one-time snapshot capture).

### Export protection
`renderMapViewImage()` must always take the definitive path; introduce a narrowly scoped flag restored in `finally` so export never consumes a live pan preview.

### MouseHandler
Pure pan detected BEFORE `updateStatusCoordinates()`/`updateHoverAndSnap()`. At left/middle pan start: `beginPanPreview()`. For every dx/dy: update viewport with existing formulas AND `shift` PanPreviewState by the same dx/dy (prefer one shared pan-shift method). At release (left and middle): finish preview, updateStatusCoordinates once, updateHoverAndSnap once, definitive repaint. Do not alter pointer work for editing/drawing/selection modes.

## Risks
- R1: snapshot cost pauses pan start → accept and measure; if unacceptable, return PARTIAL_INITIAL_SNAPSHOT_HITCH (authorizes P0-B warm-frame caching).
- R2: fixed decorations accidentally included in snapshot → keep them out.
- R3: memory churn → one reusable image, reallocate only on size change.
