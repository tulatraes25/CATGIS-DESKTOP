# CATGIS Product Certification — Tasks

Status: ACTIVE
Baseline: `bb5d854d17510a1c2518993a0c2120461f9d92a3`

## Phase P0 — Existing critical runtime gate

- [-] **P0.1 — Close pan-lag runtime validation**
  - Requirements: REQ-PC-2, REQ-PC-6, REQ-PC-11
  - Existing implementation: PR #3
  - Evidence required: exact-SHA Windows P0.8 metrics + operator interaction matrix
  - Executor: OpenCode/operator for runtime only; fixes return to GitHub
- [ ] **P0.2 — Merge/certify P0 or iterate P0-B**
  - Evidence: PR CI + post-main CI + runtime evidence

## Phase F1 — Complete inventory and truth baseline

- [ ] **F1.1 — Generate current UI/action inventory**
  - Requirements: REQ-PC-1, REQ-PC-11
  - Enumerate menus, toolbars, context actions, dialogs, loaders/exporters and action classes from source.
  - Evidence: machine/auditable inventory at exact SHA.
- [ ] **F1.2 — Build FEATURE_CERTIFICATION_MATRIX**
  - Requirements: REQ-PC-1, REQ-PC-12
  - Seed all current domains; map each visible function to source entry point and tier.
  - Old FEATURE_MATRIX/reports may inform names but cannot certify status.
- [ ] **F1.3 — Reconcile visible dead/placeholder actions**
  - Requirement: REQ-PC-1.4
  - Any visible action without executable minimum behavior becomes explicit implementation work or disabled/gated.

## Phase F2 — Automated behavior contracts

- [ ] **F2.1 — Core project/CRS/save-load contracts**
  - Requirements: REQ-PC-2, REQ-PC-3, REQ-PC-5
- [ ] **F2.2 — Vector format and editing contracts**
  - Shapefile roundtrip, GeoPackage, FlatGeobuf, SpatiaLite, CSV, DXF, editing/snapping/undo/redo.
- [ ] **F2.3 — Raster/DEM contracts**
  - Real tiny GeoTIFF/NoData/multiband/reprojection where supported.
- [ ] **F2.4 — Web service contracts**
  - Deterministic WMS/WFS XML/URL/error tests with local HTTP fixture.
- [ ] **F2.5 — PostGIS CI integration**
  - GitHub service container: connect/read/write/schema/pool/crypto/error path.
- [ ] **F2.6 — CATMAP completion**
  - Fill remaining image/table/graticule/real-project/export gaps.
- [ ] **F2.7 — Experimental dependency contracts**
  - Scripting/plugin/LAS/DWG/WCS/STAC/pgRouting minimum contract or truthful gating.

## Phase F3 — Windows end-to-end certification

- [ ] **F3.1 — Canonical E2E project workflow**
  - Requirements: REQ-PC-3
- [ ] **F3.2 — Execute every CORE matrix row on Windows**
  - Requirements: REQ-PC-2, REQ-PC-11
- [ ] **F3.3 — Execute dependency-gated rows in certified environments**
  - Requirements: REQ-PC-4, REQ-PC-8
- [ ] **F3.4 — Negative-path matrix**
  - Missing dependency, corrupt file, cancel, offline, permission/path errors.

## Phase R1 — Reliability, integrity and performance

- [ ] **R1.1 — Project corruption/recovery and atomic persistence**
  - Requirement: REQ-PC-5
- [ ] **R1.2 — Edit/undo/redo stress**
  - Requirement: REQ-PC-5
- [ ] **R1.3 — Large vector/raster/project benchmarks**
  - Requirement: REQ-PC-6
- [ ] **R1.4 — Repeated open/close resource test**
  - Requirement: REQ-PC-6
- [ ] **R1.5 — EDT/blocking-I/O audit**
  - Requirements: REQ-PC-6, REQ-PC-7
- [ ] **R1.6 — Long operation progress/cancel/error**
  - Requirements: REQ-PC-2, REQ-PC-6

## Phase A1 — Architecture hardening

Starts only after characterization coverage exists for touched behavior.

- [ ] **A1.1 — Establish executable architecture rules**
  - Requirement: REQ-PC-7
- [ ] **A1.2 — Extract blocking I/O/external adapters from UI paths**
  - Requirements: REQ-PC-6, REQ-PC-7, REQ-PC-8
- [ ] **A1.3 — Reduce MapPanel multi-responsibility surface**
  - Requirement: REQ-PC-7
- [ ] **A1.4 — Reduce LayersPanel/UI orchestration coupling**
  - Requirement: REQ-PC-7
- [ ] **A1.5 — Decompose CATMAP composer responsibilities**
  - Requirement: REQ-PC-7
- [ ] **A1.6 — Run full functional regression after each extraction**
  - Requirement: REQ-PC-2

## Phase UX1 — Final UX program

BLOCKED until F3 + R1 blocking items + A1 blocking items are certified.

- [ ] **UX1.1 — Task-flow and information-architecture audit**
  - Requirement: REQ-PC-9
- [ ] **UX1.2 — Menus/toolbars/context consistency**
- [ ] **UX1.3 — Dialog anatomy, sizing, focus and keyboard**
- [ ] **UX1.4 — Loading/progress/error/empty-state system**
- [ ] **UX1.5 — Icons, terminology, experimental/dependency labels**
- [ ] **UX1.6 — HiDPI/readability/accessibility pass**
- [ ] **UX1.7 — Full certified matrix regression after UX**

## Phase REL — Release certification

- [ ] **REL.1 — Clean-machine Windows install/start**
  - Requirement: REQ-PC-10
- [ ] **REL.2 — Dependency detection and first-run**
  - Requirements: REQ-PC-8, REQ-PC-10
- [ ] **REL.3 — Upgrade/uninstall data-safety test**
- [ ] **REL.4 — Final exact-SHA E2E + post-main CI**
  - Requirements: REQ-PC-11, REQ-PC-12
- [ ] **REL.5 — Close matrix/spec**
  - Every feature terminally classified; CORE has no FAIL/BLOCKED/UNKNOWN.

## Current next action

Finish P0.8 on PR #3, then begin F1 inventory from the newly current main without allowing UX work to bypass the functional/architecture sequence.
