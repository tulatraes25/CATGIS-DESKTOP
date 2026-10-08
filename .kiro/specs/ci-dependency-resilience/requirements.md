# CI Dependency Resilience — Requirements

Status: ACTIVE
Owner: CATGIS
Baseline: `4da6b3036d6d59c7b4726d3b906f710215c3c269`

## Goal

Restore deterministic GitHub certification after the storage optimization exposed CATGIS to cold dependency resolution failures from the external OSGeo Maven repository, while keeping GitHub Actions cache growth bounded and auditable.

## Verified incident

- B2a PR #15 was fully green before merge.
- Post-main SHA `4da6b3036d6d59c7b4726d3b906f710215c3c269` failed across Java/GIS jobs.
- Failure root cause: OSGeo Maven repository returned HTTP 502 while Gradle resolved GeoTools 34.0 / NetCDF dependencies.
- A failed-jobs rerun reproduced the same 502.
- Repository Safety and Web Viewer Lint remained green.
- Therefore this is an external dependency-availability blocker, not evidence of a CATGIS source regression.

## REQ-CDR-1 — Bounded dependency caches

WHEN Java/GIS CI resolves dependencies, THE SYSTEM SHALL use a shared dependency cache keyed only by operating system and dependency-definition hash, not by workflow/job/run SHA.

Acceptance:
- Linux jobs share one dependency cache key for a given dependency definition.
- Windows jobs share one dependency cache key for the same dependency definition.
- Cache path is limited to Gradle dependency-resolution state, not build outputs.
- The previous per-job setup-gradle cache remains disabled.

## REQ-CDR-2 — Prime once, consume offline

WHEN a workflow starts, THE SYSTEM SHALL prime the dependency cache once per required OS before downstream Java/GIS jobs execute.

Acceptance:
- A cache miss runs one explicit dependency-resolution task.
- Downstream jobs restore the primed cache and execute Gradle with `--offline`.
- A missing downstream cache is a blocking infrastructure failure.

## REQ-CDR-3 — Resolve all CATGIS test/runtime configurations

WHEN priming dependencies, THE SYSTEM SHALL resolve compile/runtime/test compile/test runtime classpaths, including those used by PostGIS integration.

## REQ-CDR-4 — Preserve storage bounds

WHEN storage maintenance runs, THE SYSTEM SHALL preserve active CATGIS dependency caches while continuing to delete unrelated regenerable caches.

Acceptance:
- keys beginning with `catgis-gradle-deps-v1-` are preserved by routine cleanup;
- setup-gradle automatic caching remains disabled;
- successful workflow artifacts remain zero;
- failure diagnostics remain bounded to 7 days.

## REQ-CDR-5 — Certification authority

WHEN this gate is complete, THE SYSTEM SHALL pass CATGIS CI and CATGIS Certification on the exact candidate SHA and again post-main. OpenCode/GUI remains blocked.
