# P0 — Pan Lag — Tasks

Status: ACTIVE / RUNTIME_VALIDATION_PENDING
Baseline: `ffe6337f738a9972e24de4785a4ed0735bbac0d4`

## P0-A-R1 finding (evidence contract)

Independent review found three evidence-contract defects in the first changeset; repaired here:

1. `snapshotMs` was structurally always zero (two identical timestamps). Fixed: `beginPanPreview` now measures real wall-clock `snapshotElapsedNanos` and stores the ELAPSED duration.
2. `fullRendersDuringPan` printed the panel lifetime counter. Fixed: the lifetime `fullSceneRenderCount` baseline is captured at snapshot completion and the diagnostic reports the session-local delta (snapshot itself not counted).
3. `fallback` was printed unconditionally as `false`. Fixed: explicit CACHED/FALLBACK outcome; invalid dimensions and non-fatal snapshot `RuntimeException` produce a truthful fallback session (`fallback=true`) while normal definitive rendering remains available.

Diagnostic format (unchanged): `[P0-PAN] snapshotMs=... previewPaints=... maxPreviewPaintMs=... fullRendersDuringPan=... fallback=true|false`.
For a valid cached session the invariant is `fallback=false` and `fullRendersDuringPan=0`.

- [x] **P0.1 — Kiro spec** (bugfix.md, design.md, tasks.md)
- [x] **P0.2 — Design** (snapshot + translation; no background rendering)
- [x] **P0.3 — Implement small coherent changeset** (PanPreviewState.java; MapPanel.java; MouseHandler.java)
- [x] **P0.4 — Performance evidence** (`[P0-PAN]` diagnostic; corrected session-accurate metrics)
- [x] **P0.5 — Tests repaired** (deterministic snapshot ms, session-local render count, truthful fallback)
- [x] **P0.6 — Build**: `gradlew.bat --no-daemon clean test` → BUILD SUCCESSFUL, 0 failures (15 pre-existing environment skips)
- [x] **P0.7 — Code audit**: P0-scoped only; no db/server/CRS/provider/unrelated-UI changes; no secrets/email/line-ending churn
- [ ] **P0.8 — Local runtime validation** (desktop GUI: empty/vector/raster/online/middle-button pans; `[P0-PAN]` evidence) — PENDING operator
- [x] **P0.9 — PR exists / CI green** (PR #3; CI 3/3 SUCCESS after R1 push)

Status remains ACTIVE / RUNTIME_VALIDATION_PENDING. P0 is not closed.
