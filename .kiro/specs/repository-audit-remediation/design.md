# Repository Audit Remediation — Design

Status: ACTIVE
Baseline: `ca980eeb1754abd1b036a57ebe3673a5267a6ce5` (main after PR #207)
Branches: `fix/main-audit` (merged, PR #206) · `fix/vector-export-atomic-publication`
(merged, PR #207) · `fix/vector-axis-order-single-normalization` (tranche 3)
Commits: `0bb839a` (web), `7c4b4d8` (catserver), `8474980` (desktop), `aca48da`
(launchers), `3c7d9d8` (docs), `5d126d5` (gitignore), `d7e4e7f` (encoding),
`d9da0e5` (verified test count), `0c335ef` + `9e1f24c` (contracts and spec) —
merged as `b2b52ff`; tranche 2 added the atomic vector export publisher and its
contracts (`8c6ba96`, `a8930f5`, `65f374a`, `9eecf75`) — merged as `ca980eeb`;
tranche 3 removes the envelope axis swap on top of `ca980eeb`.

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
3. **Documented but intentionally not patched** (see Deferred): atlas rendering
   off the EDT and CATSERVER data-model gaps. The destructive shapefile export
   (former DC-1) was fixed in tranche 2 and the envelope axis swap (former DC-2)
   in tranche 3; see *Atomic vector export* and *Envelope axis-order consistency*.

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
| New contracts | `ScaleApplyPanSyncContractTest` 2/2, `LayoutDmsDirectionContractTest` 3/3, `VectorExportPublisherContractTest` 7/7, `VectorExportRepublishContractTest` 1/1, `VectorEnvelopeAxisOrderContractTest` 5/5 |
| RED proof (scale/pan) | Removing the delegation failed both tests: mirror `10.0` vs `49.995`, zoom after pan `2.0` vs `3.9995` |
| RED proof (graticule) | Restoring the identical-branch expression failed the latitude test (1 of 3) |
| RED proof (DC-1) | Reproducing the baseline `deleteShapefileSidecars(file); exportToShapefile(...)` sequence with a failing writer left no `.shp/.shx/.dbf` for the previous dataset (data loss confirmed) |
| RED proof (DC-2) | Baseline `ShapefileData` with latitude-first CRS stored `Env[-34.62:-34.62, -58.45:-58.45]` for a point at `(-58.45, -34.62)`; contract failed with `expected: <-58.45> but was: <-34.62>` (2 of 5 cases) |
| Certification locks | `FeatureSurfaceInventoryCertificationTest` and `SemanticFeatureMatrixCertificationTest` updated and green with the new export surface registered |
| Existing export contracts | `ReleaseVectorInteropTest`, `ReleaseProjectRoundTripTest`, `ReleaseProjectRoundTripTest`, `ProjectOperationalCrsRoundTripTest`, `ReleaseFormatCompatibilityTest` green through the staging path |
| Web | `npm run lint` clean; `npm test` 31/31 |
| LF/source integrity | `JAVA_SOURCE_INTEGRITY_PASS files=751` |
| Repo policy | `.github/tests` 11/11; `tools/qa` integrity tests 8/8; PowerShell parser 9/9; repository-safety strings 5/5 |
| Wrapper | gradle-wrapper.jar sha256 matches services.gradle.org for 8.10.2 |
| NOT_VERIFIED | GitHub PR/CI/Certification/merge/attestation for tranche 3, GUI/native runtime, PostgreSQL execution, installer generation |

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
