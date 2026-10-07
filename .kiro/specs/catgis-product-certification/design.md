# CATGIS Product Certification — Design

Status: ACTIVE
Baseline: `bb5d854d17510a1c2518993a0c2120461f9d92a3`

## Current state

CATGIS already has substantial automated coverage and a broad feature set, but existing audit/release documents are dated snapshots and explicitly identify untested real-format, integration, performance, architectural and long-session paths. PR #3 contains a useful pan-lag experiment but its runtime gate is now intentionally parked until architecture certification.

The program therefore treats current GitHub + exact-SHA evidence as truth and converts the product into a feature-by-feature certification system.

## Ordering — deliberate

The required order is:

`inventory -> GitHub automated contracts -> GitHub reliability/performance -> architecture hardening/certification -> Windows E2E -> UX -> release`

Interactive program testing is intentionally deferred until the architecture gate is certified. UX is intentionally last. UX-blocking defects may be fixed earlier only when they prevent automated testing or safe execution.

## Certification matrix

Create/maintain:

`docs/quality/FEATURE_CERTIFICATION_MATRIX.md`

One row per user-facing capability, with:

| Field | Meaning |
|---|---|
| ID | stable ID, e.g. CORE-001, VEC-012 |
| Surface | menu/toolbar/dialog/context/import/export/runtime |
| Feature | user-facing capability |
| Entry point | action/dialog/class |
| Tier | CORE/BETA/EXPERIMENTAL/EXTERNAL_DEPENDENCY |
| Preconditions | data/dependency/project state |
| Automated evidence | tests + SHA |
| Windows runtime evidence | scenario + SHA |
| Negative-path evidence | cancel/error/corrupt/missing dependency |
| Performance evidence | where relevant |
| Status | certification state |
| Residual risk | explicit |

The matrix is executable program state, not marketing documentation.

## Test pyramid

### 1. Pure/unit
Use deterministic synthetic geometries, CRS, expressions, parsers, serializers and state machines.

### 2. Component/file fixture
Use tiny repository fixtures created programmatically where possible:
- Shapefile
- GeoPackage/SQLite
- GeoTIFF
- FlatGeobuf
- CSV/DXF/XML capabilities
- corrupt/truncated variants

Keep fixtures minimal, redistributable and deterministic.

### 3. Integration
Use controlled services:
- GitHub Actions PostGIS service container for read/write/transaction contracts;
- local mock HTTP server for WMS/WFS capabilities/GetMap/GetFeature parsing and error behavior;
- external-tool command seam for GDAL/Whitebox command composition;
- real external dependency validation on Windows when semantics cannot be reproduced in CI.

### 4. GitHub architecture certification
Before local runtime, GitHub must certify:
- source/package dependency rules;
- EDT/blocking-I/O boundaries;
- explicit external adapters and deterministic seams;
- hotspot characterization coverage;
- no unresolved CRITICAL/HIGH architecture finding;
- full Linux + Windows headless regression on the exact candidate SHA.

### 5. Windows E2E — locked until architecture certification
Only after `ARCHITECTURE_STATUS=CLOSED_CERTIFIED` may OpenCode synchronize the exact GitHub-tested SHA and execute local/native/runtime gates. OpenCode does not author fixes. Operator performs GUI actions only where automation cannot drive Swing reliably.

## Canonical E2E datasets

Maintain a small certified dataset pack under test resources when licensing permits:
- points/lines/polygons with attributes and known CRS;
- projected Argentine dataset;
- tiny DEM/GeoTIFF with known cells/NoData;
- corrupt/edge-case files;
- deterministic WMS/WFS XML;
- optional large benchmark datasets remain outside Git and are referenced by checksum/metadata.

## Functional domains

The initial inventory must cover at least:

1. application startup/project/CRS;
2. navigation and map rendering;
3. layer tree/visibility/order/groups;
4. vector load/create/export/style/labels/query;
5. vector editing/snapping/undo/redo/attributes;
6. raster/GeoTIFF/DEM/color/calculator/indices;
7. relief/topography/hydrology;
8. vector geoprocessing/topology/risk;
9. online XYZ/WMS/WFS/WCS/STAC/downloads;
10. PostGIS/pgRouting;
11. CAD/DXF/DWG;
12. CSV/KML/GPX/FlatGeobuf/SpatiaLite/LAS;
13. CATMAP/layout/templates/export;
14. scripting/plugins/batch;
15. help/preferences/error reporting;
16. installer/startup/dependency detection.

## Architecture target

Architecture is now a blocking pre-runtime product gate.

Do not perform a big-bang rewrite.

Characterize behavior first in GitHub, then extract seams from hotspot classes, rerunning the complete automated suite after each tranche.

Target boundaries:

`Swing UI -> application coordinators -> domain/GIS services -> ports -> adapters (GeoTools/GDAL/network/PostGIS/filesystem)`

Key rules:
- Swing classes own presentation/state coordination, not long GIS algorithms or blocking I/O.
- External adapters expose bounded operations/results and are testable behind seams.
- Map rendering state changes are EDT-safe; network/disk fetch does not execute synchronously on EDT.
- Persistence has explicit roundtrip/recovery contracts.
- Long operations expose progress/cancel/failure.
- Known hotspots (MapPanel, MapLayoutComposerDialog, LayersPanel and other multi-responsibility classes) are reduced incrementally only after characterization tests exist.

Add executable architecture checks where useful rather than relying only on class-size targets.

## UX target — final phase

The existing UI is the behavioral baseline, not the final design.

After functional/architecture closure:
1. derive task flows from the certified matrix;
2. simplify navigation around real user jobs;
3. align menus/toolbars/context actions;
4. standardize dialog anatomy and action order;
5. standardize loading/progress/error/empty states;
6. verify keyboard/focus/HiDPI;
7. run regression matrix after each UX tranche.

No UX tranche merges with unresolved functional regressions.

## Branch/CI model

Each pre-runtime coherent gate:
`exact main -> short branch -> implementation/test -> diff audit -> PR -> Linux CI + Windows CI + architecture gates -> merge -> post-main CI -> matrix/spec update`

No OpenCode transfer is required during this pre-runtime campaign.

After architecture certification, runtime gates use:
`exact certified GitHub SHA -> OpenCode sync -> Windows/native/operator evidence -> defect returns to GitHub -> new exact SHA`.

PR #3 is parked as historical implementation evidence. Do not merge it on the old runtime contract. Re-evaluate/port the pan solution against the architecture-certified baseline before runtime E2E.

## Program closure

Program status becomes CLOSED_CERTIFIED only when:
- every matrix row has a terminal certification state;
- CORE has no FAIL/BLOCKED/UNKNOWN;
- automated certification matrix has no unclassified CORE deterministic behavior;
- architecture status is CLOSED_CERTIFIED before E2E begins;
- all required E2E workflows pass after that gate;
- P0 and other runtime reliability blockers are closed on the architecture-certified baseline;
- architecture blocking rules pass;
- UX final regression passes;
- installer/clean-machine gate passes;
- exact final main SHA has post-main CI and Windows certification evidence.
