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

- [x] **F1.1 — Generate current UI/action inventory**
  - Requirements: REQ-PC-1, REQ-PC-11
  - Exact production source-surface manifest locked by `FeatureSurfaceInventoryCertificationTest`.
  - Evidence: PR #8 merged as `d0c13e2c16a25e0aa2c9f0b4b9b543d7c48ff36a`; post-main CATGIS CI run `37558982260` SUCCESS and CATGIS Certification run `37558982255` SUCCESS.
  - Certified inventory: 174 source surfaces across actions, command surfaces, dialogs/windows, exporters, loaders/readers, services and toolbars.
- [-] **F1.2 — Build FEATURE_CERTIFICATION_MATRIX**
  - Requirements: REQ-PC-1, REQ-PC-12
  - Tranche A: executable static visible-command inventory on `quality/f1-visible-command-matrix`.
  - Current source evidence: 14 command/toolbar surfaces, 390 static command occurrences, 378 source/label pairs and 313 unique labels.
  - `VisibleCommandInventoryCertificationTest` exact-match locks the inventory and emits the full TSV as CI evidence.
  - Tranche B1 CLOSED_CERTIFIED: executable source scan covers every production Java UI command/control carrier, including files missed by filename heuristics.
  - B1 baseline: 106 current UI command carriers; 89 already covered by source-surface categories and 17 supplemental carriers.
  - Evidence: PR #14 head `2e080e34093165c0efa274de22507b99ed7a83c7`; merge `757faa594793f208775205ec5d8348b07bc2d64f`; post-main CATGIS CI `37760631382` SUCCESS and Certification `37760631384` SUCCESS.
  - Tranche B2a CLOSED_CERTIFIED: 13 normalization-collision groups are explicitly mapped to stable semantic IDs by `StaticCommandAliasCertificationTest`.
  - B2a evidence: PR #15 head `236ba2a910b0a0b1b97b9700fc62d05ff2eaa659`; merge `4da6b3036d6d59c7b4726d3b906f710215c3c269`. Initial post-main Linux resolution was blocked by OSGeo HTTP 502; the same exact source SHA later passed CATGIS CI `37762106798` and Certification `37762106589` on rerun attempt 3, proving infrastructure rather than source failure.
  - Tranche B2b CLOSED_CERTIFIED: dynamic/computed menu expressions classified, non-feature factories/containers separated, 38-action module registry locked, and checkbox/radio menu commands included in the scanners.
  - B2b source ratchet: static inventory 392 occurrences / 380 source-label pairs / 315 labels; dynamic inventory 48 source-expression rows / 55 occurrences / 25 action-bearing semantic IDs; module registry 38 actions.
  - B2b evidence: PR #17 head `f218b61bd35749d0570b56643b917e541e7798be`; merge `9ec0a40814efd17c6f118b8a6ed47a278f26fa62`; post-main CATGIS CI `37774422348` SUCCESS and Certification `37774422364` SUCCESS.
  - Tranche B2c CLOSED_CERTIFIED: complete semantic matrix maps the previously inventoried static commands, dynamic actions, module actions and ACTION/DIALOG/LOADER/EXPORT source surfaces to stable semantic IDs and explicit tiers.
  - B2c certified baseline: 476 unique semantic features; 380 static command rows -> 302 semantic IDs; 38 module actions; 132 non-command source surfaces. Tier distribution: CORE 291 / BETA 123 / EXPERIMENTAL 15 / EXTERNAL_DEPENDENCY 47. All rows remain `RUNTIME_PENDING` before A0.7.
  - B2c evidence: PR #18 head `08d9fe79f689d439a1b4bc1ad1cf58ae64a9588b`; merge `8a4fda51d24a095f692ce1a29ee3a56a12804e8d`; PR CI/Certification `37809101967`/`37809102158` SUCCESS; post-main CI/Certification `37809832989`/`37809832833` SUCCESS.
  - Tranche B2d in progress: supplemental UI command carriers expose additional direct/helper-generated visible commands that were not represented in the original command scanner.
  - B2d1 executable baseline: 17 supplemental carriers, 201 command occurrences, 191 source/kind/label rows and 147 unique labels in `SUPPLEMENTAL_VISIBLE_COMMAND_INVENTORY.tsv`.
  - B2d also found three CATMAP sync menu entries without real protocol support; they are gated/disabled rather than presented as working features.
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

Certify F1.2 Tranche B2d1 supplemental visible-command inventory on GitHub, then map those supplemental commands semantically in B2d2. Only after B2d closes may F1.2 close and F1.3 begin. Do not use OpenCode/GUI until A0.7 declares `ARCHITECTURE_STATUS=CLOSED_CERTIFIED`.
