# CATGIS Product Certification — Tasks

Status: ACTIVE
Baseline: `bb5d854d17510a1c2518993a0c2120461f9d92a3`

## Phase G0 — Runtime freeze / GitHub-only certification

- [x] **G0.1 — Adopt GitHub-first transfer authority**
  - Skill rule merged and CI-certified.
- [x] **G0.2 — Defer interactive runtime until architecture certification**
  - Requirements: REQ-PC-0, REQ-PC-7
  - PR #3 is parked; exact implementation SHA remains evidence, not merge authority.
- [x] **G0.3 — Add certification CI**
  - Linux full suite, Windows full suite, skip accounting, architecture gate, PostGIS real integration, evidence artifacts.
  - Evidence: certification run `37554342919` on SHA `710073b8c2a3569b5e8e86765bc60cf49e63f589` — Linux SUCCESS, Windows SUCCESS, Architecture Guardrails SUCCESS, PostGIS real read/write/reload SUCCESS.
  - Existing CATGIS CI run `37554342803` on the same SHA — 3/3 SUCCESS.
  - Observed environment skips: 3 reviewed dependency-gated tests on Linux and Windows; no hidden `@Disabled` tests.
  - JaCoCo baseline: 753 tests, 0 failures/errors, 3 skips; line 18.03%, branch 14.61%. A no-regression floor (18% line / 14% branch) is blocking in certification CI and must ratchet upward through F2/R1.

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

## Phase A0 — Architecture certification (BLOCKING BEFORE RUNTIME)

- [ ] **A0.1 — Create executable architecture baseline**
  - Requirements: REQ-PC-0, REQ-PC-7
  - Package/source dependency rules; explicit allowlist only for known debt.
- [ ] **A0.2 — Characterize hotspot behavior before extraction**
  - MapPanel, LayersPanel, project persistence, CATMAP composer, network/external process paths.
- [ ] **A0.3 — EDT/blocking-I/O certification**
  - Network, disk and external process boundaries must not synchronously block Swing EDT.
- [ ] **A0.4 — Adapter/seam certification**
  - Network/PostGIS/GDAL/filesystem operations have deterministic test seams.
- [ ] **A0.5 — Architecture debt burn-down**
  - Zero unresolved CRITICAL/HIGH architecture findings.
- [ ] **A0.6 — Full Linux + Windows regression on architecture candidate**
  - Exact SHA, all GitHub blocking jobs green.
- [ ] **A0.7 — Declare ARCHITECTURE_STATUS=CLOSED_CERTIFIED**
  - Only this unlocks F3 runtime.

## Phase F3 — Windows end-to-end certification (LOCKED UNTIL A0.7)

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

## Phase UX1 — Final UX program

BLOCKED until A0 is CLOSED_CERTIFIED and F3/R1 runtime blocking items are certified.

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

Do not continue P0.8 locally. Build G0.3 certification CI, then execute F1/F2/R1 entirely in GitHub, harden and certify architecture in A0, and only then unlock F3/OpenCode runtime.
