# Repository Audit Remediation — Tasks

Status: TRANCHES 1–4 CERTIFIED — tranche 5 awaiting GitHub certification
Baseline: `4f47378925413163faca4adb460d68f352ae2fad` (main after PR #210)
Branches: `fix/main-audit` (merged, PR #206) · `fix/vector-export-atomic-publication`
(merged, PR #207) · `fix/vector-axis-order-single-normalization` (merged, PR #208) ·
`fix/catmap-atlas-edt-render` (merged, PR #210) ·
`fix/catserver-adaptive-database-connect` (tranche 5)

Executor vocabulary: GitHub (PR/CI/Certification/merge/attestation) · Local
(Win11 + JDK 21.0.10 + Gradle 8.10.2 + Node 26 + Python 3.14) · Manual admin
(repository administration).

## Blocking tasks

- [x] **T1 — Web viewer loads its application script**
  - Requirements: REQ-AR-1.1, AC-AR-1.1, AC-AR-1.3
  - Evidence: `public/index.html` closes the MapLibre `<script>`; test
    `index.html cierra todas las etiquetas script` asserts opening/closing
    balance; `sirve index.html y app.js como estaticos` asserts `/app.js` is
    served with the JS content type.
  - Executor: Local → GitHub pending (T14)

- [x] **T2 — Web CSP allows the MapLibre worker and the map is not silently broken**
  - Requirements: REQ-AR-1.2, AC-AR-1.2
  - Evidence: `SECURITY_HEADERS` includes `worker-src 'self' blob:` and
    `frame-ancestors 'none'`; `las cabeceras de seguridad cubren el worker blob
    de MapLibre` and `GET /api/health responde JSON con cabeceras de seguridad`
    assert both at HTTP level. MapLibre 5.22 was inspected: worker is created
    from a `blob:` URL.
  - Executor: Local → GitHub pending (T14)

- [x] **T3 — Web server fails safely at its boundary**
  - Requirements: REQ-AR-2.1…2.4
  - Evidence: `test/server.test.js` 31/31: traversal and invalid encoding
    (no file outside `public/`), directory/invalid static paths, generic 500 on
    pool failure, 403 unpublished layer, 400 for missing/invalid parameters,
    405 + `Allow`, 429 + `Retry-After`, API key 401/200, forwarded header
    ignored without trust proxy.
  - Executor: Local → GitHub pending (T14)

- [x] **T4 — Web configuration is actionable and `.env` matches documentation**
  - Requirements: REQ-AR-2.5
  - Evidence: `parseEnvContents`, `loadConfig` override and invalid-JSON tests;
    `loadEnvFile` loaded by `start()` without overriding existing variables.
  - Executor: Local → GitHub pending (T14)

- [x] **T5 — Web behavior contracts replace the placeholder and run in CI**
  - Requirements: REQ-AR-3.1…3.3
  - Evidence: `package.json` `test` = `node --test`; `catserver-web/test/server.test.js`
    31 cases; `.github/workflows/ci.yml` web job runs `npm test` after lint.
  - Executor: Local + GitHub pending (CI run in T14)

- [x] **T6 — CATSERVER SQL creates the pozos view after its base table**
  - Requirements: REQ-AR-4.1…4.3
  - Evidence: `sql/19` no longer creates `ordenamiento_territorial.ambiente_pozos_petroleros`;
    `sql/21` creates it after `control_urbano_operativo.pozos_petroleros_ejido`
    with its `COMMENT` and `GRANT SELECT`; `docs/11` documents the order and
    prerequisites. Static dependency analysis only — no PostgreSQL available.
  - Executor: Local (static) → real database run PENDING (T14 separate evidence)

- [x] **T7 — Graticule labels latitude as N/S and longitude as E/W**
  - Requirements: REQ-AR-5.2, AC-AR-5.2, AC-AR-5.3
  - Evidence: `LayoutDmsDirectionContractTest` 3/3 GREEN. RED proof: restoring
    the identical-branch expression made the latitude test fail (1 of 3).
  - Executor: Local → GitHub pending (T14)

- [x] **T8 — Status-bar scale survives a subsequent pan**
  - Requirements: REQ-AR-5.1, AC-AR-5.1, AC-AR-5.3
  - Evidence: `ScaleApplyPanSyncContractTest` 2/2 GREEN. RED proof without the
    delegation: mirror `expected 49.995 but was 10.0`; zoom after pan
    `expected 3.999500062492189 but was 2.0`.
  - Executor: Local → GitHub pending (T14)

- [x] **T9 — User-visible files use explicit UTF-8 and closed streams**
  - Requirements: REQ-AR-6.1…6.3
  - Evidence: `AttributeTableWindow` CSV export uses try-with-resources and
    reports success only after close; `MainMenuBar` reads/writes
    `.catgis-recent` with `StandardCharsets.UTF_8`; targeted SpotBugs re-run on
    the changed classes no longer reports the default-encoding findings for
    `MainMenuBar` (verified 2026-11-15).
  - Executor: Local → GitHub pending (T14)

- [x] **T10 — CATSERVER scripts stop corrupting data and misreporting status**
  - Requirements: REQ-AR-7.1…7.4
  - Evidence: `scripts/17` quotes `$quotedUserName`/`$quotedDatabase`;
    `scripts/06` writes UTF-8 without BOM (reproduced before/after by the audit
    with `Nandú`/`Ñire`); `scripts/02` reads signatures with `FileStream`
    (`cad_backup` classification reproduced); `scripts/09/11/13` check child
    `$LASTEXITCODE`; `scripts/01` sets `ErrorActionPreference=Stop`;
    `scripts/07` uses `$pgsql2shpArgs`; PowerShell parser 9/9 clean.
  - Executor: Local → GitHub pending (T14)

- [x] **T11 — Packaging launchers invoke real tasks and detect GDAL portably**
  - Requirements: REQ-AR-8.1…8.3
  - Evidence: `generar-instalador-exe.bat` now calls
    `packageWindowsJpackageExeInstaller`, which is registered in `build.gradle`;
    `probar-catgis.bat`/`run.bat` honor `CATGIS_OSGEO4W` and no longer evaluate
    a stale variable inside a parenthesized block; `README-WINDOWS.md` updated.
  - Executor: Local (inspection) → installer generation NOT_VERIFIED (T14)

- [x] **T12 — Governing documentation states the verified repository reality**
  - Requirements: REQ-AR-9.1…9.4
  - Evidence: `README.md` declares `main` canonical and records `150 refs/heads`
    and `0 tags` at 2026-11-15; `BRANCH_INVENTORY` has a dated validity note and
    closed T10 pendings; `Invoke-HistorySanitization.ps1` uses `$GitArgs` while
    all CI-validated strings remain (repository-safety 5/5 PASS);
    `TESTED_ENVIRONMENTS`/`PROJECT_OVERVIEW`/`MANUAL_TEST_PLAN` state Java 21 and
    the verified counts.
  - Executor: Local → GitHub pending (T14)

- [x] **T13 — Local gates green on the exact patched tree**
  - Requirements: REQ-AR-10.1…10.4
  - Evidence: `gradlew build` BUILD SUCCESSFUL; 1319 tests (1314 baseline + 5
    new contract cases in 2 new classes), 0 failures, 15 pre-existing skips;
    `JAVA_SOURCE_INTEGRITY_PASS files=747`; `npm run lint` clean and `npm test`
    31/31; `.github/tests` 11/11; `tools/qa` 8/8; PowerShell parser 9/9; wrapper
    sha256 matches the official Gradle 8.10.2 checksum.
  - Executor: Local

- [x] **T14 — GitHub certification of tranche 1 (PR #206)**
  - Requirements: REQ-AR-10, program rule GITHUB-first
  - Evidence: PR [#206](https://github.com/tulatraes25/CATGIS-DESKTOP/pull/206)
    from `fix/main-audit` against `main` with exact base `aecb4d2`; PR head
    `9e1f24ca8112296e71328d1b47493ff69029c7fc` with **30/30 checks PASS**
    (CI 43726623191, Certification 43726623257 — Linux/Windows/macOS suites,
    PostGIS real, architecture guardrails, inventories, app images, native GDAL —
    Runtime Identity 43726623184 8/8, GeoParquet 43726623210, PR attestation
    43726623317); merged with `--match-head-commit` as
    `b2b52ff6633b1dda115b4d0ebcd9d988b22c1819`; post-main CI 43727739019,
    Certification 43727739034, Runtime Identity 43727739033 and GeoParquet
    43727739108 all SUCCESS; post-main attestation run 43728666255 SUCCESS with
    `ATTEST_BINDING_PASS` and 26 attested PASS jobs.
  - Executor: GitHub + local follow-up (completed)
  - This closed the only blocking gate of tranche 1; real PostGIS execution of
    `sql/21` was exercised by the certified PostGIS job, installer generation
    remains NOT_VERIFIED.

- [x] **T15 — RED→GREEN regression evidence recorded**
  - Requirements: AC-AR-5.3, program rule "no fabricated PASS"
  - Evidence: see T7 and T8; both contracts were executed against the unfixed
    code and failed with the documented symptom before being restored.
  - Executor: Local

- [x] **T16 — Certification ledger reconciled with current main**
  - Requirements: REQ-AR-9.5
  - Evidence: dated reconciliation block appended to
    `.kiro/specs/catgis-product-certification/tasks.md` (baseline `bb5d854` is
    historical; current main `aecb4d2`; the "Current next action" STAC item is
    present in main with a contract test and still needs certification evidence).
  - Executor: Local → GitHub pending (T14)

- [x] **T17 — STAC pagination contract reflects its real state**
  - Requirements: REQ-AR-9.5
  - Evidence: `docs/quality/STAC_COLLECTION_PAGINATION_CONTRACT.md` records the
    implementation and tests present on `aecb4d2` while keeping
    `NOT_CERTIFIED` until GitHub gates close.
  - Executor: Local → GitHub pending (T14)

- [x] **T18 — Residual defect register emitted (not fixed by this spec)**
  - Requirements: spec transparency rule
  - Evidence: register below; each item needs its own spec/issue before a fix,
    because each requires multi-file atomic publication, empirical GIS data or a
    product/data-model decision.
  - Executor: Local (register) → follow-up specs

- [x] **T19 — Spec self-review (autonomous phase progression)**
  - Requirements: `spec-kiro.md` autonomous progression
  - Checklist: requirements complete and non-contradictory (yes, 11 REQs with
    acceptance); design covers every REQ (traceability table); every task maps to
    a requirement; rollback and security addressed; no destructive operation
    added; evidence exists for every `[x]`.
  - Executor: Local

- [x] **T20 — Atomic vector export publisher (DC-1)**
  - Requirements: REQ-AR-11, AC-AR-11.1…11.4
  - Evidence: `VectorExportPublisher` implements same-volume staging; single
    files are published with `ATOMIC_MOVE` (fallback `REPLACE_EXISTING`);
    shapefiles require `.shp/.shx`, back up existing parts, restore on failure and
    delete backups on success. `deleteShapefileSidecars` was removed and both
    export branches of `ExportVectorLayerAction` now publish through the staging
    path. `VectorExportPublisherContractTest` 7/7 GREEN: single-file failure keeps
    the previous file byte-identical, success replaces it, shapefile failure keeps
    the whole previous set, success replaces it and drops stale parts, missing
    mandatory parts are rejected, new datasets are created, and no `.catgis-*`
    temporaries remain. RED evidence: reproducing the baseline
    `deleteShapefileSidecars(file); exportToShapefile(...)` sequence with a
    failing writer left no `.shp/.shx/.dbf` (data loss confirmed).
  - Executor: Local → GitHub pending (T22)

- [x] **T21 — Real shapefile republish contract and certification locks**
  - Requirements: REQ-AR-11.5, AC-AR-11.6
  - Evidence: `VectorExportRepublishContractTest` 1/1 GREEN — a real GeoTools
    export over an existing dataset publishes the second version (feature
    attribute `Pozo B`/`9`), the schema keeps its attributes and no temporaries
    remain. Existing export suites (`ReleaseVectorInteropTest`,
    `ReleaseProjectRoundTripTest`, `ProjectOperationalCrsRoundTripTest`,
    `ReleaseFormatCompatibilityTest`) stay green through the staging path. The new
    surface is registered in `docs/quality/SOURCE_FEATURE_SURFACE_INVENTORY.tsv`
    and `docs/quality/SURFACE_SEMANTIC_MAP.tsv`
    (`SEM-SURFACE-EXPORTVECTORLAYERACTION`, CORE), so
    `FeatureSurfaceInventoryCertificationTest` and
    `SemanticFeatureMatrixCertificationTest` pass. Full suite: 1327 tests, 0
    failures, 15 pre-existing skips.
  - Executor: Local → GitHub pending (T22)

- [x] **T22 — GitHub certification of tranche 2 (PR #207)**
  - Requirements: REQ-AR-11, program rule GITHUB-first
  - Evidence: PR [#207](https://github.com/tulatraes25/CATGIS-DESKTOP/pull/207)
    from `fix/vector-export-atomic-publication` against `main` `b2b52ff`; PR head
    `9eecf75cfd46c1f3bb361571097c1670bb86c138` with **30/30 checks PASS**
    (CI 43731049657, Certification 43731049826, Runtime Identity 43731049668
    8/8, GeoParquet 43731049700, PR attestation 43731049685); merged with
    `--match-head-commit` as `ca980eeb1754abd1b036a57ebe3673a5267a6ce5`;
    post-main CI 43732244120, Certification 43732244126, Runtime Identity
    43732244230 and GeoParquet 43732244146 all SUCCESS; post-main attestation
    run 43733562098 SUCCESS with `ATTEST_BINDING_PASS` and 26 attested PASS jobs.
  - Executor: GitHub + local follow-up (completed)

## Tranche 3 — DC-2 envelope axis order

- [x] **T23 — Remove the envelope axis swap (DC-2)**
  - Requirements: REQ-AR-12, AC-AR-12.1…12.4
  - Evidence: `ShapefileData` no longer normalizes the envelope in any
    constructor; `ShapefileLoader` and `GeoPackageLoader` pass the raw bounds;
    `CRSDefinitions.normalizeEnvelopeAxisOrder`/`isLatLonAxisOrder` removed as
    misleading dead helpers. RED evidence on baseline `ShapefileData`: a point
    stored at `(-58.45, -34.62)` with latitude-first CRS produced
    `Env[-34.62:-34.62, -58.45:-58.45]` and failed with
    `expected: <-58.45> but was: <-34.62>` (2 of 5 contract cases).
  - Executor: Local → GitHub pending (T25)

- [x] **T24 — Axis-order behavior contract**
  - Requirements: AC-AR-12.5
  - Evidence: `VectorEnvelopeAxisOrderContractTest` **5/5 GREEN** (latitude-first
    constructor, longitude-first constructor, feature-collection constructor,
    real Shapefile fixture, real GeoPackage fixture). Full suite: 1332 tests, 0
    failures, 15 pre-existing skips (217 suites).
  - Executor: Local → GitHub pending (T25)

- [x] **T25 — GitHub certification of tranche 3 (PR #208)**
  - Requirements: REQ-AR-12, program rule GITHUB-first
  - Evidence: PR [#208](https://github.com/tulatraes25/CATGIS-DESKTOP/pull/208)
    from `fix/vector-axis-order-single-normalization` against `main` `ca980eeb`;
    PR head `9b3432e644ac44f497cff39c3a8d493c86e8b1cb` with **30/30 checks PASS**
    (CI 43738305867, Certification 43738305868, Runtime Identity 43738305854
    8/8, GeoParquet 43738305907, PR attestation 43738305864); merged with
    `--match-head-commit` as `84553751eb404e0662cf3ce8892371ffaa7ca7a6`;
    post-main CI 43739450407, Certification 43739450323, Runtime Identity
    43739450324 and GeoParquet 43739450296 all SUCCESS; post-main attestation
    run 43740579263 SUCCESS with `ATTEST_BINDING_PASS` and 26 attested PASS jobs.
  - Executor: GitHub + local follow-up (completed)

## Tranche 4 — DC-3 EDT serialization of export renders

- [x] **T26 — Serialize export renders on the EDT (DC-3)**
  - Requirements: REQ-AR-13, AC-AR-13.1…13.4
  - Evidence: new `EdtRenderSupport` marshals render work to the EDT and runs
    inline when already there; the three export seams now use it
    (`LayoutMap.captureMapImage`, `MapFrameRenderer.render` delegation to the
    live panel, `LayoutPageRenderer` page render). `MapPanel` was left at its
    certified size (3489 lines, ceiling 3500) because the architecture ratchet
    forbids growing it. RED evidence: disabling the dispatch left the export
    render painting off the EDT and the contract failed with
    `expected: <true> but was: <false>`.
  - Executor: Local → GitHub pending (T28)

- [x] **T27 — EDT render contracts**
  - Requirements: AC-AR-13.1…13.5
  - Evidence: `EdtRenderSupportContractTest` 4/4 (background marshal, inline on
    EDT, unchanged failure propagation, null-task rejection) and
    `ExportRenderEdtContractTest` 1/1 (a `MapFrameRenderer` export render issued
    from a background worker paints on the EDT and never off it). Existing
    `MapPanelRenderMapViewImageTest`, `MapPanelRenderStateCharacterizationTest`
    and `MapPanelPanFramePreviewContractTest` stay green. Full suite: 1337 tests,
    0 failures, 15 pre-existing skips (219 suites).
  - Executor: Local → GitHub pending (T28)

- [x] **T28 — GitHub certification of tranche 4 (PR #210)**
  - Requirements: REQ-AR-13, program rule GITHUB-first
  - Evidence: PR [#210](https://github.com/tulatraes25/CATGIS-DESKTOP/pull/210)
    from `fix/catmap-atlas-edt-render` against `main` `8455375`; PR head
    `ecaaab157591da04625eeb2b2528cc87067f9b37` with **30/30 checks PASS**
    (CI 43746289195, Certification 43746289396, Runtime Identity 43746289179
    8/8, GeoParquet 43746289204, PR attestation 43746289158); merged with
    `--match-head-commit` as `4f47378925413163faca4adb460d68f352ae2fad`;
    post-main CI 43747510374, Certification 43747510316, Runtime Identity
    43747510365 and GeoParquet 43747510561 all SUCCESS; post-main attestation
    run 43748542492 SUCCESS with `ATTEST_BINDING_PASS` and 26 attested PASS jobs.
    An earlier PR #209 was closed because its branch was based on the tranche-3
    head instead of live main; the attestation gate rejected it
    (`PR_HEAD_NOT_DESCENDED_FROM_LIVE_MAIN`) and the branch was recreated from
    main without force-push.
  - Executor: GitHub + local follow-up (completed)

## Tranche 5 — DC-4 adaptive CATSERVER database

- [x] **T29 — Stop forcing the CATSERVER database name**
  - Requirements: REQ-AR-14, AC-AR-14.1…14.4
  - Evidence: all 19 stage scripts (`sql/01`–`sql/21`, clustered into 01-15 and
    18-21) lost their hardcoded `\connect` and now include
    `\common/assert_catserver_database.sql`, a shared guard that rejects any
    database other than `catserver`/`CATSERVER` with an explicit message.
    `sql/02` and `sql/12` build database-level privileges from
    `current_database()` via `format('%I', …)`.
    `scripts/03_run_bootstrap_psql.ps1` gained `-Database` and targets the
    maintenance database for `sql/00` and the created database for `sql/01`–`04`.
    `docs/11` and `docs/06` document the resolved behaviour and both scenarios.
  - Executor: Local → GitHub pending (T31)

- [x] **T30 — Static SQL contract in the required safety check**
  - Requirements: AC-AR-14.6
  - Evidence: `tools/qa/check_catserver_sql_contract.py`
    (`CATSERVER_SQL_CONTRACT_PASS files=20`) and
    `tools/qa/test_catserver_sql_contract.py` (7/7 disposable-fixture tests:
    clean tree, `\connect` rejection, missing guard, hardcoded database
    privilege, permissive guard, bootstrap without canonical database, empty
    tree). Both run in the required **Repository Safety Checks** job.
  - Executor: Local → GitHub pending (T31)

- [ ] **T31 — GitHub certification of tranche 5 (BLOCKING for closure)**
  - Requirements: REQ-AR-14, AC-AR-14.5
  - Required evidence: PR from `fix/catserver-adaptive-database-connect` against
    `main` with exact base `4f47378`; PR-head CI + Certification (including the
    new PostGIS SQL pipeline step) + Runtime Identity + GeoParquet SUCCESS;
    merge with expected head SHA; post-main runs SUCCESS and post-main
    attestation PASS.
  - Executor: GitHub (credentialed account) + local follow-up

## Deferred defect register

| ID | Severity | Defect | Why deferred |
|---|---|---|---|
| DC-1 | ~~HIGH~~ RESOLVED | `ExportVectorLayerAction.saveLayerToFile` deleted shapefile sidecars (`.shp/.shx/.dbf/.prj…`) **before** exporting; a failed export destroyed the user's source file | Resolved by T20/T21 (PR pending in T22): same-volume staging, backup/restore publication and 8 deterministic contracts |
| DC-2 | ~~MEDIUM~~ RESOLVED | Double axis-order normalization: `ShapefileLoader`/`GeoPackageLoader` normalized the envelope and `ShapefileData` normalized again, cancelling the swap for some CRS; with latitude-first CRS metadata other constructors stored a swapped envelope | Resolved by T23/T24 (PR pending in T25): envelope is always the stored (x, y) bounds and 5 deterministic cases lock it |
| DC-3 | ~~MEDIUM~~ RESOLVED | CATMAP atlas generation called `MapPanel.renderMapViewImage` from a background thread, mutating live Swing state/unsynchronized caches | Resolved by T26/T27 (PR pending in T28): export seams marshal renders to the EDT with `EdtRenderSupport` without growing `MapPanel` |
| DC-4 | ~~HIGH~~ RESOLVED | CATSERVER: `sql/12–21` forced `\connect catserver` while the bootstrap creates `"CATSERVER"`; a fresh install aborted at script 12 | Resolved by T29/T30 (PR pending in T31): no script forces a database, the shared guard rejects non-CATSERVER databases, and both names are exercised in the PostGIS job |
| DC-5 | HIGH | CATSERVER: `catastro.ejido_urbano_mask`, required by `sql/10/11/18`, is created by no SQL or script | Requires a data-source decision and a documented import step |
| DC-6 | MEDIUM | CATSERVER: table/vista name collision `catastro.circunscripcion_sector` (04 table vs 18 view) | Model decision |
| DC-7 | MEDIUM | CATSERVER: inconsistent role/default-privilege policies between `sql/02`, `sql/12`, `sql/19` | Needs a single ownership model before editing grants |
| DC-8 | MEDIUM | CATSERVER: `DROP`/`TRUNCATE`/`-overwrite` destructive paths without backup or transactional wrapper | Operational policy decision |
| DC-9 | LOW | 150 inherited remote branches remain; README now documents them instead of claiming a single branch | Repository administration (manual admin) |
| DC-10 | LOW | 4 npm advisories in dev-only tooling (`brace-expansion`/`minimatch` via `eslint`) | Dependency upgrade deliberately excluded from a bugfix-only change set |
| DC-11 | LOW | Historical certification specs (`requirements.md`, `t10c-support-handoff.md`) contain superseded ruleset/repository IDs | Historical records; only a dated reconciliation was added, not a rewrite |
| DC-12 | LOW | 15 skipped Java tests and 3 reviewed environment skips remain pre-existing | Skip inventory must not grow; reducing it is separate certification work |

## Blocker / dependency notes

- Tranches 1 (T14), 2 (T22), 3 (T25) and 4 (T28) are certified on GitHub.
  Tranche 5 still needs its own exact-PR and post-main certification cycle (T31);
  local runs do not certify it.
- No local PostgreSQL/PostGIS instance: the CATSERVER pipeline evidence runs in
  the certification PostGIS job (real bootstrap on `"CATSERVER"`, lowercase
  `catserver` and guard rejection).
- GUI/native runtime, installer generation and Windows script execution require
  the authorized local phase after A0.7 and are explicitly NOT_VERIFIED.
- A0.7 remains **NOT_CERTIFIED**; this spec does not claim otherwise.

## Current next action

Certify tranche 5 on GitHub (T31: PR `fix/catserver-adaptive-database-connect`
against `main` `4f47378`, exact PR CI/Certification, merge with
`expected_head_sha`, post-main runs and attestation). After that, the remaining
deferred high-severity item is CATSERVER DC-5 (`catastro.ejido_urbano_mask` has
no producer), which needs a data-source decision before any code change.
