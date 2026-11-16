# Repository Audit Remediation — Design

Status: CLOSED_CERTIFIED (2026-11-15)
Baseline: `f965869a8ef73e36afae0de6218cd4191ce8a151` (main after PR #215)
Branches: `fix/main-audit` (merged, PR #206) · `fix/vector-export-atomic-publication`
(merged, PR #207) · `fix/vector-axis-order-single-normalization` (merged, PR #208) ·
`fix/catmap-atlas-edt-render` (merged, PR #210) ·
`fix/catserver-adaptive-database-connect` (merged, PR #211) ·
`fix/catserver-ejido-mask-fail-closed` (merged, PR #212) ·
`port/205-vector-source-crs` (merged, PR #213) ·
`port/204-project-state-authority` (merged, PR #214) ·
`port/202-specialized-layer-identity` (merged, PR #215)
Commits: `0bb839a` (web), `7c4b4d8` (catserver), `8474980` (desktop), `aca48da`
(launchers), `3c7d9d8` (docs), `5d126d5` (gitignore), `d7e4e7f` (encoding),
`d9da0e5` (verified test count), `0c335ef` + `9e1f24c` (contracts and spec) —
merged as `b2b52ff`; tranche 2 added the atomic vector export publisher and its
contracts (`8c6ba96`, `a8930f5`, `65f374a`, `9eecf75`) — merged as `ca980eeb`;
tranche 3 removed the envelope axis swap (`a45b2e9`, `a2aa6fe`, `9b3432e`) —
merged as `8455375`; tranche 4 serialized export renders on the EDT (`6ed72c4`,
`c757a8a`, `08a81d1`, rebased as `9b2f041`, `080a176`, `ecaaab1`) — merged as
`4f47378`; tranche 5 made the CATSERVER SQL database-agnostic (`0ef8703`,
`fc43ba8`, `f0bc789`, `dea8f46`, `a594702`) — merged as `f363ace`; tranche 6
failed closed on the missing ejido mask (`4853fc1`, `646830d`, `1eac5b7`,
`df0917f`) — merged as `530abf2`; tranche 7 rescued the historical open PRs onto
live main (`d5b008f`, `3ac2e0c`, `b994d0b`) — merged as `963c561`, `0c73e8f`
and `f965869`.
Closure: every blocking task is certified on GitHub at the exact PR heads and
post-main SHAs; the decision-gated remainder is transferred to
`.kiro/specs/catserver-decision-backlog/`.

## Current state

The audit began on an outdated clone (`947d39f`, branch `perf/p0-pan-lag`):
`origin/main` was 637 commits ahead (`aecb4d2`) and the remote holds 150
branches. The work was therefore re-based on the exact current main and every
finding was re-validated before being fixed. Three classes of outcome resulted:

1. **Still present on main and fixed here**: web viewer load/CSP/boundary,
   CATSERVER SQL ordering and script safety, graticule direction, scale/pan
   mirror sync, launcher task name, documentation truth.
2. **Already fixed upstream and dropped** (not re-implemented): STAC pagination
   (`visitedPages` + cap), atomic project save (`ProjectFilePublisher`),
   bounded project open (`ProjectOpenRecordIntegrity`), pan-preview rework,
   `GeoParquetReader` resource handling.
3. **Documented but intentionally not patched** (see Deferred): CATSERVER
   data-model gaps. The destructive shapefile export (former DC-1) was fixed in
   tranche 2, the envelope axis swap (former DC-2) in tranche 3, the off-EDT
   atlas render (former DC-3) in tranche 4 and the hardcoded CATSERVER database
   name (former DC-4) in tranche 5. The missing ejido mask (DC-5) is mitigated
   fail-closed in tranche 6; deciding which script must *produce* it remains a
   data-source decision.

## Target state

A branch that applies only the confirmed, re-validated repairs on the current
main, keeps every existing local gate green on the exact patched tree, adds
deterministic behavior contracts for the two user-visible regressions, and
records dated reconciliation for the stale governing documents. GitHub PR/CI/
Certification and all runtime evidence remain outside this branch.

## Architecture / components

| Area | Components | Commit |
|---|---|---|
| Web viewer | `public/index.html`, `server.js` (createApp/createServer), `test/server.test.js`, `.github/workflows/ci.yml` | `0bb839a` |
| CATSERVER | `sql/19`, `sql/21`, `scripts/01,02,06,07,09,11,13,17`, `docs/11`, `docs/03,06,10`, `README` | `7c4b4d8` |
| Desktop behavior | `layout/LayoutMap`, `MapEditingEngine`, `AttributeTableWindow`, `OfflineRoutingEngine`, `DwgImportSupport`, `GeometryTools`, `RasterCalculatorEngine`, `MapFrameRenderer`, `build.gradle` | `8474980`, `d7e4e7f` |
| Launchers | `generar-instalador-exe.bat`, `probar-catgis.bat`, `run.bat`, `README-WINDOWS.md` | `aca48da` |
| Governing docs | `README.md`, `docs/repository/*`, `catgis-desktop/docs/*` | `3c7d9d8`, `d9da0e5` |
| Spec artifacts | this spec, certification-ledger reconciliation, STAC contract status | current commit |

### Web server boundary

`server.js` is split into `createApp({pool, config, logger, apiKey, trustProxy,
rateLimit})` and `createServer(app)`; `start()` owns configuration, the pool and
the listener. This keeps production behavior while making every route testable
without a database: the single seam is `pool.query(sql, params)`.

- Static serving resolves inside `public/` and rejects traversal, invalid
  encoding, directories and stream errors with 400/404, never an unhandled
  exception.
- API authorization uses `crypto.timingSafeEqual` over SHA-256 digests of the
  supplied and expected key.
- `X-Forwarded-For` is honored only with `CATSERVER_WEB_TRUST_PROXY`.
- `HttpError` carries the intended status (400/403/404/405/429) and the handler
  maps unknown failures to a generic 500 while logging the real cause.
- `.env` is loaded without overriding existing environment variables, matching
  the documented behavior; `config/local.json` parse errors are actionable.

### Scale/pan synchronization

`MouseHandler` pan mutates the legacy `MapPanel.viewMinX/viewMinY/zoomFactor`
mirror and then calls `syncViewToController()`. `MapPanel.applyZoom`,
`handleZoom`, `zoomIn` and `zoomOut` all call `syncViewFromController()` after
touching the controller; `MapEditingEngine.applyZoom` (used by the status-bar
scale path) did not. The fix delegates to `MapPanel.applyZoom`, which already
performs panel-size sync, controller update and mirror sync in the same order as
the other navigation paths.

### Graticule direction

`LayoutMap.formatGridCoordinate`/`formatDMS` are shared by both axes; the
direction expression previously had two identical branches, so Y-axis labels
were emitted as E/W. The axis is now propagated explicitly as `isLongitude`
(X → E/W, Y → N/S). `formatDMS` is package-private only to allow the contract
test; behavior is unchanged.

### Atomic vector export (DC-1)

`ExportVectorLayerAction.saveLayerToFile` and `saveLayerToFileWithTarget` used to
call `deleteShapefileSidecars(file)` **before** exporting. A failure after that
point destroyed the user's dataset; the RED reproduction with a failing writer
shows the previous `.shp/.shx/.dbf` already gone.

Every export now goes through `VectorExportPublisher`:

- single-file formats (dxf/geojson/kml/kmz/gpx) write into a temporary file in
  the destination directory and are moved into place with `ATOMIC_MOVE`
  (fallback `REPLACE_EXISTING`); the temporary file is always removed;
- shapefiles write into a temporary directory on the same volume, require at
  least `.shp`/`.shx`, back up the existing parts to `.catgis-old-<token><ext>`,
  move the staged parts into place, restore the backup on any publication
  failure, and delete backups plus staging on success;
- `deleteShapefileSidecars` was removed; both export branches share the same
  publisher so the `Ctrl+S` project-save path and the explicit export path are
  protected identically;
- `VectorExportPublisher` is registered in
  `docs/quality/SOURCE_FEATURE_SURFACE_INVENTORY.tsv` and mapped to the parent
  `SEM-SURFACE-EXPORTVECTORLAYERACTION` feature in
  `docs/quality/SURFACE_SEMANTIC_MAP.tsv`, following the existing
  `CatmapAtomicExport -> LayoutExportEngine` precedent, so the architecture
  certification locks stay green.

### Envelope axis-order consistency (DC-2)

`ShapefileData` used to call `CRSDefinitions.normalizeEnvelopeAxisOrder`, which
swapped min/max X/Y whenever the schema CRS declared a latitude-first axis order
(`AXIS["Geodetic latitude", NORTH]` first, as some ESRI `.prj` files or the
`CRS.decode` fallback produce). JTS envelopes are the (x, y) bounds of the stored
coordinates, so swapping them made the layer envelope disagree with its own
geometry. The shapefile and GeoPackage loaders additionally pre-normalized the
envelope, so on those two paths the swap cancelled out; every other constructor
(used by the DXF/KML/GPX/GeoJSON/FlatGeobuf/PostGIS loaders and by editing code)
received a swapped envelope for latitude-first metadata.

RED evidence on baseline `ShapefileData`: one point stored at `(-58.45, -34.62)`
produced `dataEnvelope=Env[-34.62:-34.62, -58.45:-58.45]`, and the contract failed
with `expected: <-58.45> but was: <-34.62>`.

Tranche 3 keeps envelopes untouched in `ShapefileData`, removes the redundant
loader pre-normalization, removes the misleading
`CRSDefinitions.normalizeEnvelopeAxisOrder`/`isLatLonAxisOrder` helpers, and locks
the invariant with `VectorEnvelopeAxisOrderContractTest` (five cases:
latitude-first constructor, longitude-first constructor, feature-collection
constructor, real Shapefile fixture, real GeoPackage fixture).

### EDT serialization of export renders (DC-3)

CATMAP atlas generation runs on a background `new Thread` and eventually calls
`MapPanel.renderMapViewImage` through `LayoutMap` (which resolves the live
`AppContext.mapPanel()`) and through `MapFrameRenderer` when the project has
raster/online layers. The same seam is used by layout export and preview
snapshots. That method mutates live panel state (`viewMinX/viewMinY/zoomFactor`,
panel size, `layoutRenderMode`, the exporting flag and the temporary render
order) and calls `paintComponent`, so calling it from a worker race against the
normal EDT paint cycle could corrupt view state or produce wrong atlas pages.

Tranche 4 routes the three export seams through `EdtRenderSupport.call(...)`:
`LayoutMap.captureMapImage`, the `MapFrameRenderer.render` delegation to the live
panel, and `LayoutPageRenderer`'s page render. The helper follows the repo's
architecture ratchet: it does not grow `MapPanel` (which is at its certified
ceiling); it guards the export boundaries instead.

- callers already on the EDT run inline (no deadlock, no extra round-trip);
- background callers are marshalled with `invokeAndWait` and receive the image,
  the original failure, or a clear interruption error (interrupt flag restored);
- `MapPanel` keeps its certified size and render contracts unchanged.

RED evidence: disabling the dispatch left the background export render painting
off the EDT and the contract failed with
`El render de exportacion debe pintar en el Event Dispatch Thread. ==> expected: <true> but was: <false>`. The
architecture ratchet stayed green because `MapPanel` was reverted to its
certified size (3489 lines, ceiling 3500).

### Adaptive CATSERVER database selection (DC-4)

The stage-2 SQL files hardcoded `\connect catserver` while `sql/00` creates the
database as `"CATSERVER"`. On a fresh install each stage-2 script aborted with
`database "catserver" does not exist` before running a single statement, and the
internal `\connect` also overrode the caller's `psql -d`.

Tranche 5 removes the hardcoded connection from every stage script (19 files)
and replaces it with `\ir _common/assert_catserver_database.sql`, a shared guard
that raises an explicit error unless `lower(current_database()) = 'catserver'`.
The database-level privilege statements in `sql/02` and `sql/12` now build their
target from `current_database()` through `format('%I', ...)`, so the same SQL
works on both historical names without renaming any installation. The bootstrap
orchestrator (`scripts/03_run_bootstrap_psql.ps1`) gained a `-Database`
parameter: `sql/00` runs against the maintenance database and `sql/01`–`sql/04`
against the created database.

Validation is executable in GitHub:

- `tools/qa/check_catserver_sql_contract.py` (plus seven disposable-fixture unit
  tests) rejects any `\connect`, missing guard or hardcoded database privilege
  statement, and runs inside the required Repository Safety check;
- the PostGIS certification job runs the real orchestrator to bootstrap
  `"CATSERVER"`, applies `sql/12` there, repeats `sql/01`, `02` and `12` on a
  lowercase `catserver`, and proves the guard rejects an unrelated database with
  the expected message.

### Fail-closed ejido mask prerequisite (DC-5)

`sql/10`, `sql/11` and `sql/18` filter with `catastro.ejido_urbano_mask`, but no
repository artifact creates that table: on a clean database the failure was a
late `relation "catastro.ejido_urbano_mask" does not exist` in the middle of a
load, after some derived tables were already dropped.

Tranche 6 adds `_common/assert_ejido_mask.sql`, a shared guard included at the
top of those three scripts, which raises a specific actionable error when:

- the table is missing (`falta catastro.ejido_urbano_mask`),
- the `geom` column is missing,
- `geom` is not in SRID 4326,
- the `gid` column is missing (needed because `sql/13`/`sql/18` publish the
  table as a viewer layer).

`docs/11` now states the full contract table, the `ogr2ogr` command with
`-lco FID=gid`, and explicitly keeps the *producer* decision open. The static
contract checker requires the mask guard for every script that consumes the
mask, and validates the guard content. The PostGIS job proves three outcomes
against the real database: missing table rejected with the expected message,
wrong SRID rejected with the SRID message, and the valid contract letting the
script proceed until the next missing prerequisite (`staging.cad_barrios_lineas_src`).

## Sequence

1. Synchronize the clone to the exact main SHA and re-validate each finding.
2. Port fixes whose files are unchanged upstream; drop fixes already upstream.
3. Re-apply in-place fixes that are still needed on current code (LayoutMap,
   MapEditingEngine, AttributeTableWindow, MainMenuBar, build.gradle message).
4. Add deterministic contracts for the user-visible regressions.
5. Prove RED (contract fails without the repair) and GREEN (passes with it).
6. Run local gates: Java build/suite, web lint+test, PowerShell parser,
   repository-safety and attestation unit tests, Java LF integrity.
7. Record dated reconciliation for the certification ledger and the STAC
   contract, then commit atomically per area.
8. Open the GitHub PR stage (outside this branch): CI + Certification on the PR
   head, merge with expected head SHA, post-main attestation.

## Security and privacy

- No secrets, tokens, private keys or personal paths are introduced; the secret
  scan over tracked files was clean (only commented `CAMBIAR` placeholders).
- API-key comparison no longer leaks through early exit; forwarded headers are
  opt-in; error responses do not expose internal text.
- New tests bind loopback sockets only (`127.0.0.1`, ephemeral port) and no
  external service; the CATSERVER SQL was not executed and no credential was
  handled.
- The Java LF gate (`tools/qa/check_java_source_integrity.py`) proves every
  committed `.java` is LF-only and the checkout matches the blob.

## Validation

| Gate | Evidence |
|---|---|
| Java build + suite (tranche 1) | BUILD SUCCESSFUL, 1319 tests (1314 baseline + 5 new contract cases), 0 failures, 15 pre-existing skips |
| Java build + suite (tranche 2) | BUILD SUCCESSFUL, 1327 tests (1319 + 8 DC-1 contract cases), 0 failures, 15 pre-existing skips |
| Java build + suite (tranche 3) | BUILD SUCCESSFUL, 1332 tests (1327 + 5 DC-2 contract cases), 0 failures, 15 pre-existing skips |
| Java build + suite (tranche 4) | BUILD SUCCESSFUL, 1337 tests (1332 + 5 DC-3 contract cases), 0 failures, 15 pre-existing skips; architecture hot-spot ratchet green with `MapPanel` at 3489/3500 lines |
| New contracts | `ScaleApplyPanSyncContractTest` 2/2, `LayoutDmsDirectionContractTest` 3/3, `VectorExportPublisherContractTest` 7/7, `VectorExportRepublishContractTest` 1/1, `VectorEnvelopeAxisOrderContractTest` 5/5, `EdtRenderSupportContractTest` 4/4, `ExportRenderEdtContractTest` 1/1 |
| RED proof (scale/pan) | Removing the delegation failed both tests: mirror `10.0` vs `49.995`, zoom after pan `2.0` vs `3.9995` |
| RED proof (graticule) | Restoring the identical-branch expression failed the latitude test (1 of 3) |
| RED proof (DC-1) | Reproducing the baseline `deleteShapefileSidecars(file); exportToShapefile(...)` sequence with a failing writer left no `.shp/.shx/.dbf` for the previous dataset (data loss confirmed) |
| RED proof (DC-2) | Baseline `ShapefileData` with latitude-first CRS stored `Env[-34.62:-34.62, -58.45:-58.45]` for a point at `(-58.45, -34.62)`; contract failed with `expected: <-58.45> but was: <-34.62>` (2 of 5 cases) |
| RED proof (DC-3) | Disabling the EDT dispatch left the `MapFrameRenderer` export delegation painting off the EDT; contract failed with `El render de exportacion debe pintar en el Event Dispatch Thread. ==> expected: <true> but was: <false>` |
| CATSERVER SQL contract (tranche 5) | `CATSERVER_SQL_CONTRACT_PASS files=20`; contract unit tests 7/7; no Java changes, so the Java suite remains 1337 tests / 0 failures from tranche 4; PowerShell parser clean for the modified orchestrator |
| PostGIS pipeline evidence | The certification PostGIS job bootstraps `"CATSERVER"` through `scripts/03_run_bootstrap_psql.ps1`, applies `sql/12` there, repeats `sql/01`/`02`/`12` on a lowercase `catserver`, and proves the guard rejects an unrelated database (run IDs in T31) |
| Ejido mask fail-closed (tranche 6) | `CATSERVER_SQL_CONTRACT_PASS files=20 mask_consumers=3`; contract unit tests 10/10; PostGIS job proves missing-table rejection, wrong-SRID rejection and valid-contract continuation (run IDs in T34) |
| Certification locks | `FeatureSurfaceInventoryCertificationTest` and `SemanticFeatureMatrixCertificationTest` updated and green with the new export surface registered |
| Existing export contracts | `ReleaseVectorInteropTest`, `ReleaseProjectRoundTripTest`, `ReleaseProjectRoundTripTest`, `ProjectOperationalCrsRoundTripTest`, `ReleaseFormatCompatibilityTest` green through the staging path |
| Web | `npm run lint` clean; `npm test` 31/31 |
| LF/source integrity | `JAVA_SOURCE_INTEGRITY_PASS files=754` |
| Repo policy | `.github/tests` 11/11; `tools/qa` integrity tests 8/8; PowerShell parser 9/9; repository-safety strings 5/5 |
| Wrapper | gradle-wrapper.jar sha256 matches services.gradle.org for 8.10.2 |
| NOT_VERIFIED | GitHub PR/CI/Certification/merge/attestation for tranche 5, GUI/native runtime, installer generation |

## Rollback

Each commit is atomic and revertible in isolation (`git revert <sha>` on the
branch). The branch never mutates `main`, performs no force-push and adds no
irreversible migration: the CATSERVER change moves a view creation to the script
that owns its base table (a re-run creates the same object), and the web server
keeps its public routes and response shapes. The atomic export publisher is
additive: reverting it restores the previous export behavior and leaves no
staged state behind, because every publication cleans up its own staging and
backup files.

## Risks / alternatives

- **Dropping upstream-fixed items** (STAC, project save/open, pan preview) is
  deliberate: re-porting stale patches would duplicate certified work. The
  corresponding contract document is updated with its real state instead.
- **The two new Java contracts depend on `ReleaseTestSupport` and headless
  Swing**; they are deterministic (no timing assertions, no display server) and
  follow the existing `MapPanelNavigationHistoryContractTest` pattern.
- **Windows script fixes are parser- and inspection-verified only**; no Windows
  execution of `ogr2ogr`/`psql`/`jpackage` was possible in this phase.
- **CATSERVER SQL** cannot be executed without PostGIS; the ordering fix is
  justified by dependency analysis in `docs/11` and remains PENDING for a real
  database run.
- **Deferred defects** carry higher residual risk than the ones fixed here; they
  need their own spec/issue because they require multi-file atomic publication
  or user-model decisions (see tasks `T18`).
- **Atomic export limitation**: staging and backups use hidden
  `.catgis-*` names inside the destination directory. A hard crash between the
  backup and the final move can leave inert `.catgis-old-*` files next to the
  dataset; loaders never read them and the next successful export ignores them,
  but a future cleanup task may sweep them. Cross-volume or network-share
  renames may degrade to `REPLACE_EXISTING`, which is still a single-file
  replace but not guaranteed atomic by the filesystem.
- **GeoTools regenerates index/`.cpg` parts when reading** a shapefile, so the
  integration contract asserts the published content and the absence of
  `.catgis-*` temporaries instead of pinning a specific regenerated sidecar.
- **Envelope axis order (DC-2)**: the app now assumes every reader returns
  geometry coordinates in stored (x, y) order, which is what Shapefile,
  GeoPackage and the other GeoTools readers do. A dataset whose coordinates were
  genuinely stored latitude-first would need a coordinate transform, not an
  envelope swap; that remains out of scope and no repo fixture behaves that way.
- **EDT serialization cost (DC-3)**: each atlas page or layout export render now
  occupies the EDT for the duration of that page. The atlas flow is modal, so the
  user cannot trigger conflicting edits while it runs, but a very heavy page can
  briefly freeze the UI. A future improvement is rendering from immutable
  viewport snapshots without touching the live `MapPanel`; the architecture
  ratchet forbids growing `MapPanel` to absorb that work.
- **Adaptive CATSERVER database (DC-4)**: the guard accepts only
  `catserver`/`CATSERVER`; an installation with another physical name must be
  migrated explicitly (documented as a separate migration, never a silent
  rename). The stage scripts rely on the psql `\ir` metacommand, which matches
  the documented psql-only pipeline. The certification job creates a `postgres`
  role to mirror a default PostgreSQL install because `sql/12` sets default
  privileges for that role (tracked as DC-7).
- **Ejido mask (DC-5)**: the guard makes the missing prerequisite loud, but the
  mask must still be imported manually; which script should produce it, from
  which source, and with which attributes remains an open data-source decision.
  A future producer must satisfy the documented `gid`/`geom`/SRID 4326 contract.

## Traceability

| Requirement | Design section | Tasks |
|---|---|---|
| REQ-AR-1 | Web viewer / Web server boundary | T1, T2 |
| REQ-AR-2 | Web server boundary | T2, T3, T4 |
| REQ-AR-3 | Web server boundary | T5 |
| REQ-AR-4 | CATSERVER | T6 |
| REQ-AR-5 | Scale/pan synchronization; Graticule direction | T7, T8, T15 |
| REQ-AR-6 | Sequence step 3 | T9 |
| REQ-AR-7 | CATSERVER | T10 |
| REQ-AR-8 | Launchers | T11 |
| REQ-AR-9 | Governing docs / Sequence step 7 | T12, T16, T17 |
| REQ-AR-10 | Validation | T13, T14, T19 |
| REQ-AR-11 | Atomic vector export (DC-1) | T20, T21, T22 |
| REQ-AR-12 | Envelope axis-order consistency (DC-2) | T23, T24, T25 |
| REQ-AR-13 | EDT serialization of export renders (DC-3) | T26, T27, T28 |
| REQ-AR-14 | Adaptive CATSERVER database selection (DC-4) | T29, T30, T31 |
| REQ-AR-15 | Fail-closed CATSERVER data prerequisites (DC-5) | T32, T33, T34 |
