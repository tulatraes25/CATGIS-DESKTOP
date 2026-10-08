# GitHub Storage Optimization — Requirements

Status: ACTIVE
Owner: CATGIS
Baseline: `963133c6d585cfde8bc2ea377a35af34b031321b`

## Goal

Reduce CATGIS GitHub storage consumption without deleting functional source, required runtime resources, or irreplaceable certification evidence. Prevent future GitHub Actions storage growth from repeated Gradle caches and redundant test artifacts.

## Verified baseline

- GitHub repository metadata size: approximately **10.25 MB**.
- Current checked-out tree payload: approximately **15.85 MB**.
- Largest tracked file: `catgis-desktop/src/help/docs/CATGIS_Manual_Profesional_2026_FINAL.docx` at approximately **8.1 MB**.
- That DOCX is referenced by `HelpCenterDialog`; it is therefore not classified as removable in this gate.
- GitHub Actions history: **39 workflow runs** at baseline.
- Existing workflow artifacts reconstructed from all current runs: approximately **155.8 MB** total.
- Gradle Actions logs show repeated repository cache activity on push runs, including shared caches around **97–130 MB** plus per-job Gradle state caches around **5 MB**. Cache inventory size cannot be queried directly through the connected GitHub API surface, so workflow logs are the authoritative evidence for cache amplification.

## REQ-GSO-1 — Preserve product truth

WHEN reducing GitHub storage, THE SYSTEM SHALL preserve all source code, tests, runtime resources and product documentation that are currently referenced by production/runtime code.

Acceptance:
- No production Java source is deleted.
- No test source or deterministic fixture is deleted solely for storage reduction.
- The bundled professional manual DOCX remains because it is referenced by HelpCenter.
- No Git history rewrite is performed in this gate.

## REQ-GSO-2 — Stop Gradle cache amplification

WHEN CATGIS CI or Certification runs, THE SYSTEM SHALL NOT create new Gradle Actions cache entries until a future explicit performance/storage trade-off gate re-enables caching.

Acceptance:
- Every `gradle/actions/setup-gradle` step in active workflows declares caching disabled.
- CI remains functionally equivalent except for potentially longer dependency setup time.
- Tests and certification behavior remain blocking.

## REQ-GSO-3 — Minimize test artifact retention

WHEN workflow evidence is generated, THE SYSTEM SHALL retain only evidence that is useful for diagnosis or exact-SHA certification and SHALL avoid persistent duplicate HTML/report bundles from successful PR runs.

Acceptance:
- Successful pull-request CI does not upload routine Java diagnostics or JaCoCo bundles.
- Successful pull-request Certification does not upload routine evidence bundles.
- Failed runs may upload diagnostics.
- Successful post-main runs may retain certification evidence for a short bounded retention window.
- Retention for newly uploaded workflow artifacts is explicitly bounded to no more than 7 days in this gate.

## REQ-GSO-4 — One-time storage purge

WHEN the storage optimization change lands on `main` with the bootstrap marker, THE SYSTEM SHALL delete regenerable GitHub Actions caches and existing workflow artifacts.

Acceptance:
- Purge uses repository-scoped GitHub Actions APIs only.
- Purge does not delete workflow runs, commits, source, PRs, issues, releases or Git history.
- The purge is idempotent if no caches/artifacts remain.

## REQ-GSO-5 — Ongoing bounded storage

AFTER bootstrap cleanup, THE SYSTEM SHALL periodically remove stale workflow artifacts and Actions caches so storage cannot grow without bound.

Acceptance:
- A scheduled maintenance workflow runs weekly.
- Stale artifacts older than 7 days are deleted.
- Actions caches are deleted by scheduled maintenance because normal CATGIS workflows no longer depend on them.
- Manual dispatch remains available.

## REQ-GSO-6 — Certification safety

WHEN this gate changes CI/storage behavior, THE SYSTEM SHALL pass existing CATGIS CI and CATGIS Certification before merge.

Acceptance:
- No runtime/UX validation is required.
- OpenCode remains blocked.
- Existing functional/architecture tests remain unchanged.
- Exact PR head SHA and post-main SHA are recorded in the task ledger after certification.
