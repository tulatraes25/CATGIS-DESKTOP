# Repository Audit Remediation — Tasks

Status: LOCAL_VERIFIED_ENGINEERING / GITHUB_PENDING (NOT_CERTIFIED)
Baseline: `aecb4d20621bb5065f8acb388813eefe594f3a27` (main, PR #199)
Branch: `fix/main-audit`

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

- [ ] **T14 — GitHub certification of this fix set (BLOCKING for closure)**
  - Requirements: REQ-AR-10, program rule GITHUB-first
  - Required evidence: PR from `fix/main-audit` against `main` with exact base
    `aecb4d2`; PR-head CI + Certification SUCCESS; merge with expected head SHA;
    post-main CI + Certification SUCCESS; post-merge attestation. Real PostGIS
    execution of `sql/21` and installer generation also remain NOT_VERIFIED here.
  - Executor: GitHub (credentialed account) + Manual admin
  - Status: **PENDING — no push credentials/authorization in this session.**

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
  - Checklist: requirements complete and non-contradictory (yes, 10 REQs with
    acceptance); design covers every REQ (traceability table); every task maps to
    a requirement; rollback and security addressed; no destructive operation
    added; evidence exists for every `[x]`.
  - Executor: Local

## Deferred defect register

| ID | Severity | Defect | Why deferred |
|---|---|---|---|
| DC-1 | HIGH | `ExportVectorLayerAction.saveLayerToFile` deletes shapefile sidecars (`.shp/.shx/.dbf/.prj…`) **before** exporting; a failed export destroys the user's source file and there is no rollback | Requires multi-file atomic publication (temp directory + atomic replace) and export tests; not a one-line fix |
| DC-2 | MEDIUM | Double axis-order normalization: `ShapefileLoader`/`GeoPackageLoader` normalize the envelope and `ShapefileData` normalizes again, cancelling the swap for some CRS | Needs empirical axis-order proof with real datasets before changing; a wrong fix breaks `zoomToLayer` |
| DC-3 | MEDIUM | CATMAP atlas generation calls `MapPanel.renderMapViewImage` and mutates live Swing state/unsynchronized caches from a background thread | Architectural: serialize renders or take immutable viewport snapshots |
| DC-4 | HIGH | CATSERVER: `sql/12–21` `\connect catserver` while the bootstrap creates `"CATSERVER"`; a fresh install aborts at script 12 | Data-model/naming migration decision documented in `docs/11`; renaming an existing install is explicitly out of scope |
| DC-5 | HIGH | CATSERVER: `catastro.ejido_urbano_mask`, required by `sql/10/11/18`, is created by no SQL or script | Requires a data-source decision and a documented import step |
| DC-6 | MEDIUM | CATSERVER: table/vista name collision `catastro.circunscripcion_sector` (04 table vs 18 view) | Model decision |
| DC-7 | MEDIUM | CATSERVER: inconsistent role/default-privilege policies between `sql/02`, `sql/12`, `sql/19` | Needs a single ownership model before editing grants |
| DC-8 | MEDIUM | CATSERVER: `DROP`/`TRUNCATE`/`-overwrite` destructive paths without backup or transactional wrapper | Operational policy decision |
| DC-9 | LOW | 150 inherited remote branches remain; README now documents them instead of claiming a single branch | Repository administration (manual admin) |
| DC-10 | LOW | 4 npm advisories in dev-only tooling (`brace-expansion`/`minimatch` via `eslint`) | Dependency upgrade deliberately excluded from a bugfix-only change set |
| DC-11 | LOW | Historical certification specs (`requirements.md`, `t10c-support-handoff.md`) contain superseded ruleset/repository IDs | Historical records; only a dated reconciliation was added, not a rewrite |
| DC-12 | LOW | 15 skipped Java tests and 3 reviewed environment skips remain pre-existing | Skip inventory must not grow; reducing it is separate certification work |

## Blocker / dependency notes

- No GitHub credentials in this session: push, PR, CI, Certification, merge and
  post-main attestation cannot be executed here; they are the only remaining
  blocking gate for closure (T14).
- No PostgreSQL/PostGIS instance: SQL changes are dependency-analysis verified,
  not executed.
- GUI/native runtime, installer generation and Windows script execution require
  the authorized local phase after A0.7 and are explicitly NOT_VERIFIED.
- A0.7 remains **NOT_CERTIFIED**; this spec does not claim otherwise.

## Current next action

Publish `fix/main-audit` (8 commits + this spec) as a draft PR against `main`
`aecb4d2`, obtain exact PR CI/Certification and post-merge attestation (T14),
then schedule DC-1 (destructive shapefile export) as its own GitHub-first spec,
followed by CATSERVER DC-4/DC-5 before any fresh-install certification claim.
