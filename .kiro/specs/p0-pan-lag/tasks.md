# P0 — Pan Lag — Tasks

Status: ACTIVE
Baseline: `ffe6337f738a9972e24de4785a4ed0735bbac0d4`

- [ ] **P0.1 — Kiro spec** (bugfix.md, design.md, tasks.md)
- [ ] **P0.2 — Design** (snapshot + translation; no background rendering)
- [ ] **P0.3 — Implement small coherent changeset**
  - NEW `catgis-desktop/src/ar/com/catgis/PanPreviewState.java`
  - MODIFY `catgis-desktop/src/ar/com/catgis/MapPanel.java`
  - MODIFY `catgis-desktop/src/ar/com/catgis/MouseHandler.java`
  - `MoveTool.java` only if needed to remove duplicated state without behavior change
- [ ] **P0.4 — Performance evidence**: `[P0-PAN]` diagnostic at session end (snapshotMs, previewPaints, maxPreviewPaintMs, fullRendersDuringPan, fallback). Keep EMERGENCY-PERF logging.
- [ ] **P0.5 — Tests**
  - NEW `catgis-desktop/src/test/java/ar/com/catgis/PanPreviewStateTest.java`
  - NEW `catgis-desktop/src/test/java/ar/com/catgis/MapPanelPanPreviewTest.java` (headless; counter-based)
- [ ] **P0.6 — Build**: `gradlew.bat --no-daemon clean test` → BUILD SUCCESSFUL, 0 failures
- [ ] **P0.7 — Code audit**: no db/server/unrelated UI/CRS/provider/CATMAP-behavior changes; no secrets/email/line-ending churn/generated files
- [ ] **P0.8 — Local runtime validation**: empty/vector/raster/online/middle-button pans; blocking acceptance on [P0-PAN]
- [ ] **P0.9 — Commit/PR** (local noreply commits; push branch; PR; 3/3 CI; DO NOT MERGE)
