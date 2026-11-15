# Repository Audit Remediation — Bugfix

Status: ACTIVE
Owner: CATGIS
Baseline: `aecb4d20621bb5065f8acb388813eefe594f3a27` (main, PR #199)
Branch: `fix/main-audit`
Program: BUGFIX_ONLY — feature freeze respected (no new features, no dependency upgrades, no GUI/OpenCode validation)

## Goal

Remove the confirmed defects found by a full-repository audit of CATGIS Desktop,
CATSERVER, the web viewer, its launchers and its governing documentation, with
executable evidence per defect, without changing certified behavior on success
paths and without mutating `main` directly.

The audit started from an outdated clone (`947d39f`, 637 commits behind main,
which still contained the `perf/p0-pan-lag` line). Every finding was therefore
re-validated against main `aecb4d2` before being fixed; findings already solved
upstream were dropped instead of re-implemented.

## Non-goals

- No new product features, no feature-surface changes, no UX redesign.
- No dependency upgrades (only the existing lockfiles are used).
- No GUI, native-runtime, packaging-installer or OpenCode validation. Those need
  an explicitly authorized local phase and A0.7, and are covered by the product
  certification program, not by this spec.
- No PostgreSQL/PostGIS execution: CATSERVER SQL is reviewed and ordered
  statically. Real database execution remains pending.
- No cleanup of the 150 inherited remote branches and no GitHub administrative
  action (requires repository administration).
- No rewrite of historical certification records beyond dated reconciliation
  notes.

## REQ-AR-1 — Web viewer must load and render

WHEN a browser opens the CATSERVER Web viewer, THE SYSTEM SHALL load its
application script and allow the map renderer to start.

Acceptance:
- AC-AR-1.1: `public/index.html` closes every `<script>` element it opens, so
  `/app.js` is fetched and executed.
- AC-AR-1.2: The served Content-Security-Policy allows the MapLibre worker
  (`worker-src 'self' blob:`) and the map tiles/styles actually requested.
- AC-AR-1.3: A test asserts the script-tag balance and the CSP worker rule.

## REQ-AR-2 — Web server boundary must fail safely

WHEN the web server receives an invalid, unauthorized, oversized or unavailable
request, THE SYSTEM SHALL answer with the correct HTTP status and SHALL NOT
crash, leak files outside `public/`, or leak internal error text.

Acceptance:
- AC-AR-2.1: Static requests are contained in `public/`; traversal, invalid
  encoding and directories produce 400/404 without process termination.
- AC-AR-2.2: Stream/stat errors never produce an unhandled exception; pooled
  database errors are logged and answered with a generic 500.
- AC-AR-2.3: Unpublished layers answer 403, invalid `gid` answers 400, missing
  parameters 400, unsupported methods 405 with `Allow`.
- AC-AR-2.4: API-key comparison is timing-safe; `X-Forwarded-For` is only
  trusted when `CATSERVER_WEB_TRUST_PROXY` is enabled.
- AC-AR-2.5: Malformed local configuration produces an actionable message
  instead of an unhandled parse failure.

## REQ-AR-3 — Web behavior contracts must be executable

WHEN `npm test` runs in CI, THE SYSTEM SHALL execute a real automated suite for
the web server.

Acceptance:
- AC-AR-3.1: The placeholder `npm test` script is replaced by `node --test`.
- AC-AR-3.2: The suite covers configuration, parameter validation, catalog
  caching, layer authorization, search, rate limit, API key, static serving,
  traversal attempts and error paths.
- AC-AR-3.3: The CI web job runs `npm test` in addition to the existing lint.

## REQ-AR-4 — CATSERVER SQL must be executable in declared order

WHEN the numbered CATSERVER scripts run on a clean cluster, THE SYSTEM SHALL
create each object only after its dependencies exist.

Acceptance:
- AC-AR-4.1: `ordenamiento_territorial.ambiente_pozos_petroleros` is created by
  the script that builds its base table, not before it.
- AC-AR-4.2: The view keeps its `COMMENT` and `GRANT SELECT` after relocation.
- AC-AR-4.3: The ordering contract is documented with prerequisites.

## REQ-AR-5 — Desktop map and label behavior must be correct

WHEN the user applies a view scale from the status bar and pans, THE SYSTEM
SHALL preserve the applied scale; WHEN the layout graticule labels geographic
coordinates, THE SYSTEM SHALL label longitude as E/W and latitude as N/S.

Acceptance:
- AC-AR-5.1: Applying a status-bar scale synchronizes the legacy view mirror
  with `MapViewController`; a subsequent pan does not revert the zoom.
- AC-AR-5.2: Graticule labels for the Y axis use N/S; X axis uses E/W.
- AC-AR-5.3: Both behaviors have deterministic headless JUnit contracts.

## REQ-AR-6 — Resources and text encoding must be explicit

WHEN CATGIS writes user-visible files or CSV exports, THE SYSTEM SHALL close
each stream on every path and SHALL use an explicit UTF-8 encoding.

Acceptance:
- AC-AR-6.1: CSV export uses try-with-resources; success status is reported only
  after the stream closes.
- AC-AR-6.2: The recent-files file is read and written as UTF-8.
- AC-AR-6.3: No new default-charset or leaked-stream findings appear in the
  audited classes.

## REQ-AR-7 — CATSERVER scripts must not corrupt or misreport

WHEN a CATSERVER PowerShell script imports, exports, batches or provisions,
THE SYSTEM SHALL fail loudly on error and SHALL preserve non-ASCII data.

Acceptance:
- AC-AR-7.1: SQL identifiers interpolated in scripts are quoted; a name with
  quotes cannot break or inject the statement.
- AC-AR-7.2: KML/PGDump import preserves accented attributes (UTF-8, no BOM).
- AC-AR-7.3: Batch scripts check child exit codes and report `ERROR`.
- AC-AR-7.4: Scripts that create directories stop on failure; `$args` is not
  used as a parameter name; all edited scripts parse in PowerShell 5.1 and 7.

## REQ-AR-8 — Launchers and packaging must point to real tasks

WHEN a user runs a packaging launcher, THE SYSTEM SHALL invoke a Gradle task
that exists and SHALL detect GDAL through documented, portable paths.

Acceptance:
- AC-AR-8.1: `generar-instalador-exe.bat` invokes the registered jpackage task.
- AC-AR-8.2: GDAL detection honors `CATGIS_OSGEO4W` and no longer evaluates a
  stale variable inside a parenthesized block.
- AC-AR-8.3: `README-WINDOWS.md` documents JDK 21, the wrapper, jpackage+WiX
  and the signing variables.

## REQ-AR-9 — Governing documentation must tell the truth

WHEN documentation states a repository state, THE SYSTEM SHALL state the
verified state or mark it as a dated historical record.

Acceptance:
- AC-AR-9.1: `README.md` no longer claims that `main` is the only remote branch;
  it declares `main` canonical and records the observed ref state.
- AC-AR-9.2: `BRANCH_INVENTORY` carries a dated validity note and the
  sanitation pendings are marked closed per T10B–T10E.
- AC-AR-9.3: `Invoke-HistorySanitization.ps1` follows the repository's own rule
  (`$GitArgs`, not `$Args`) while preserving every CI-validated string.
- AC-AR-9.4: `TESTED_ENVIRONMENTS`, `PROJECT_OVERVIEW` and `MANUAL_TEST_PLAN`
  state Java 21 and the verified test count.
- AC-AR-9.5: The product-certification ledger records a dated reconciliation of
  its stale baseline, and the STAC pagination contract reflects its real state
  on main.

## REQ-AR-10 — Repository gates must remain green locally

WHEN the fix set is complete, THE SYSTEM SHALL keep every existing local gate
green on the exact patched SHA.

Acceptance:
- AC-AR-10.1: `gradlew build` succeeds and the full Java suite has zero failures.
- AC-AR-10.2: `tools/qa/check_java_source_integrity.py` reports PASS and every
  committed `.java` is LF-only with a matching checkout.
- AC-AR-10.3: `npm run lint` and `npm test` pass; repository-safety and
  attestation unit tests pass; the Gradle wrapper checksum still matches.
- AC-AR-10.4: The pre-existing skip inventory does not grow because of this
  change set.

## Constraints

- GitHub `main` plus exact-SHA CI remains the implementation truth; this branch
  never mutates `main` and is not certified by these local runs.
- No local parallel rewrite of behavior: fixes are minimal and preserve success
  paths.
- No secrets, personal paths, proprietary data or heavy binaries are added.
- Evidence is recorded per task; a task with no evidence stays open.
