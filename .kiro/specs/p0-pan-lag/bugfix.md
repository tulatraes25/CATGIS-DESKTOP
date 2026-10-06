# P0 — Pan Lag / Fluid Map Navigation — Bugfix Spec

Status: ACTIVE
Baseline: `ffe6337f738a9972e24de4785a4ed0735bbac0d4`
Repo: `tulatraes25/CATGIS-DESKTOP`
Branch: `perf/p0-pan-lag`

## BUG

Interactive map pan is not acceptably fluid because expensive map rendering can execute repeatedly on the Swing EDT during dragging. A pan repaint can trigger a complete geographical scene render (online tiles/WMS, local raster, vector layers, labels, heatmap, clusters, selections/editing overlays/pins/sketches, decorations) for every drag event.

## REQ-P0-1 — Fluid preview

WHEN the user actively pans the map, THE SYSTEM SHALL update the visible map position without requiring a complete geographical scene render for every mouse-drag event.

Acceptance:
- AC-P0-1.1: A valid pan-preview session performs zero complete scene renders between snapshot capture and pan release.
- AC-P0-1.2: Successive drag deltas are represented immediately by bitmap translation.

## REQ-P0-2 — Final viewport correctness

WHEN pan ends, THE SYSTEM SHALL preserve the exact viewport produced by the accumulated drag deltas and perform a definitive render from that viewport.

Acceptance:
- AC-P0-2.1: Final world extent equals the extent produced by existing pan math.
- AC-P0-2.2: The post-release definitive frame is not the stale preview frame.

## REQ-P0-3 — Interaction parity

THE SYSTEM SHALL preserve: normal left-button pan; temporary middle-button pan; zoom history semantics; drawing; measurement; feature editing; selection box; pin dragging; CAD placement; topographic profile capture.

## REQ-P0-4 — Fixed screen UI

WHILE map content is translated during pan preview, screen-fixed decorations SHALL remain screen-fixed (scale/decorations, attribution). They must not visually travel with the map bitmap.

## REQ-P0-5 — Pointer work

WHILE pure pan is active, THE SYSTEM SHALL NOT perform cursor CRS/status transformation or snap search for every drag event. It SHALL update pointer/status state once after pan completes.

## REQ-P0-6 — Export isolation

Map export / CATMAP rendering through `renderMapViewImage()` SHALL NOT reuse an active screen pan-preview bitmap.

## REQ-P0-7 — EDT/network safety

The fix SHALL NOT introduce: synchronous network access on EDT; background rendering against mutable Swing Graphics; uncontrolled render threads.

## REQ-P0-8 — Bounded memory

Pan preview buffering SHALL use at most one reusable panel-sized image, reallocating only when dimensions require it.

## Hypotheses

- H1 (PRIMARY): Repeated complete map rendering on the EDT during active pan is the dominant cause of continuous pan lag. Falsifier: if translated-cached-bitmap panning still feels slow, H1 is insufficient.
- H2 (SECONDARY): updateStatusCoordinates / CRS conversion and snap processing add avoidable EDT work during pure navigation. Falsifier: if removed and mouseDragged still spends >=75 ms without full renders, another bottleneck exists.

## Non-goals

- Do not move GeoTools rendering to another thread in this phase.
- Do not refactor unrelated rendering architecture.

## P0-A-R1 — Evidence contract correction

The `[P0-PAN]` evidence must be session-accurate:

- `snapshotMs` = real wall-clock duration of the one-time snapshot (elapsed, not a zero-width timestamp);
- `fullRendersDuringPan` = definitive scene renders between snapshot completion and pan release (session-local delta; the snapshot itself is not counted);
- `fallback` = truthful CACHED/FALLBACK outcome (invalid dimensions or non-fatal snapshot failure ⇒ `fallback=true`).

For a valid cached session: `fallback=false` and `fullRendersDuringPan=0` (AC-P0-1.1).
