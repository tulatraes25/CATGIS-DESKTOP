# CATGIS A0.2 — Hotspot Behavior Characterization

Status: **PARTIAL / NOT_CERTIFIED**
Base GitHub SHA: `9cd767d9555c095f6481cf824b6c835f13d63fac`
Gate: architecture remains **OPEN**; do not unlock OpenCode/GUI.

## Why this matters

Architecture refactoring is blocked until existing visible behavior is locked with
deterministic tests on GitHub. Large source files and line-count ceilings alone
are **not evidence** that refactoring will preserve product behavior.

## MapPanel export/render state — Tranche A

Executable contract: `MapPanelRenderStateCharacterizationTest`.

- A renderer exception must propagate without mutating the interactive map's
  viewport fields, layout rendering flag, panel dimensions, preferred size or
  temporary layer order override.
- A subsequent successful render must remain possible after a failed render.
- Invalid zoom must reject rendering without invoking the painter or changing
  interactive state.
- All Swing mutations in this contract run on the EDT through
  `ReleaseTestSupport.runOnEdt`.
- The existing `MapPanelRenderMapViewImageTest` continues to certify the
  requested image size and normal-path temporary ordering.

This characterization does **not** certify the full renderer, large datasets,
frame timing or GUI appearance.

## Required remaining A0.2 work (not passed by Tranche A)

| Hotspot | Next behavioral characterization |
|---|---|
| `MapPanel` | Zoom/history, selection, redraw and mixed raster/vector layer state |
| `LayersPanel` | Deterministic group ordering, filter, selection, drag/drop and cancel |
| `LoadProjectAction` | Project reconstruction, partial read failure and rollback |
| `MapLayoutComposerDialog` / CATMAP | Layout geometry, template reload, real map/export and frame consistency |
| `MapEditingEngine` | Geometry edit undo/redo/cancel, snapping and invalid geometries |
| Online/external process paths | EDT isolation and deterministic cancellation/errors |

## Exit criteria

A0.2 closes only when relevant hotspot behavior is characterized across those
surfaces, new tests pass on Linux/Windows CI, and the architectural review
identifies safe extraction seams. Passing this tranche alone is **not**
`ARCHITECTURE_STATUS=CLOSED_CERTIFIED`.
