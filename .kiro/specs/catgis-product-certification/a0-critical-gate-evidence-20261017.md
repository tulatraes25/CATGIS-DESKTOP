# A0.5 / A0.7 — Evidence-based blockers (2026-10-17)

Status: **NOT_CERTIFIED**. Baseline `de080ce87480b9716d11d751d3f03679f0f2334e`.
Purpose: make the existing architecture certification gate explicit without widening the scope or claiming that draft PRs are merged.

## Verified source-level blockers on baseline

1. `catgis-desktop/src/ar/com/catgis/LayerVectorDataSupport.java`, `ensureDataLoaded`: the `.fgb` and `.las/.laz` branches set `sourceCRS=EPSG:4326` when blank. This can make unreferenced projected coordinates appear authoritative. The GeoJSON/KML defaults are format-specific and must not be indiscriminately removed. Related: #158, #151, #169.
2. Same file, `loadLasFile`: `LasReader.readPoints(file, 50000)` applies an unconditional prefix limit without persistent partial-preview provenance on the returned layer. No professional full-dataset semantics can be inferred from the returned features. Related: #176 / PR #180.
3. The A0 task ledger still marks A0.2 and A0.3 partial; A0.4, A0.5, A0.6 and A0.7 uncompleted. Existing architecture ratchets prevent regression but do not prove zero CRITICAL/HIGH defects.

## Required closure sequence

- Integrate correction PRs on the **current** main, resolving overlaps and verifying real CRS/no-CRS, reprojection, fail-closed paths and project reload. Keep provenance explicit.
- For LAS, first certify strict rejection of malformed/unsupported/overlarge files without silent success (PR #180), then retain #176 for bounded streaming + typed preview/full-data provenance. An over-limit rejection is containment, not large-data feature completion.
- Validate A0.2 hotspot contracts (map state, layer operations, project rollback, CATMAP, editing) and A0.3 EDT process/network/disk paths against source; implement deterministic failures and cancellation tests before declaring complete.
- A0.4: inventory deterministic seams for filesystem, GDAL, WFS/WMS and PostGIS, with actual boundary assertions.
- A0.5: review and close all open critical/high source findings on the exact candidate. Do not count an open PR as a fixed main defect.
- A0.6: certify exact-head and exact-main Linux/Windows regression, macOS Intel/ARM suites and native dependencies, plus verified skip inventory and fresh push atestation.
- Only then declare A0.7 `ARCHITECTURE_STATUS=CLOSED_CERTIFIED`; F3 physical GUI/Windows workflow remains separate.

## Non-goals

No automatic CRS assignment to unknown data; no claim that LAZ is supported; no full-LiDAR/FlatGeobuf scale claim; no PC/OpenCode/GUI testing, release, force push, or direct main mutation. This is an evidence inventory, **not** an implementation or A0.7 certification.
