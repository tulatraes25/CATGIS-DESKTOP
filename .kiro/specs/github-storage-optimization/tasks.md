# GitHub Storage Optimization — Tasks

Status: ACTIVE
Baseline: `963133c6d585cfde8bc2ea377a35af34b031321b`

- [x] **GSO.1 — Measure current storage sources**
  - Requirements: REQ-GSO-1, REQ-GSO-2, REQ-GSO-3
  - Evidence:
    - repository metadata ~10.25 MB;
    - current tree ~15.85 MB;
    - 39 Actions runs;
    - current workflow artifacts ~155.8 MB;
    - Gradle logs prove repeated cache restores/writes including ~97–130 MB shared entries and ~5 MB per-job state entries.
  - Decision: do not delete runtime-referenced manual DOCX.

- [x] **GSO.2 — Stop new Gradle cache writes**
  - Requirement: REQ-GSO-2
  - Add `cache-disabled: true` to all active Gradle setup steps.

- [x] **GSO.3 — Bound workflow artifact creation**
  - Requirement: REQ-GSO-3
  - Successful PR runs should not persist routine diagnostic/evidence bundles.
  - Failed jobs retain useful diagnostics.
  - Successful post-main evidence retention <= 7 days.

- [x] **GSO.4 — Add repository storage maintenance workflow**
  - Requirements: REQ-GSO-4, REQ-GSO-5
  - PR dry-run validates cache/artifact API access with read-only permissions.
  - Bootstrap marker: `[storage-cleanup]`.
  - Bootstrap deletes all current Actions caches and artifacts.
  - Weekly/manual maintenance deletes all caches and artifacts older than 7 days.

- [x] **GSO.5 — PR certification**
  - Requirement: REQ-GSO-6
  - Audit exact diff.
  - PR #11 head `8e9c1eadc321b8be39bc2e4d5627d35bd92bdb94` passed CATGIS CI, CATGIS Certification and storage API validation.

- [x] **GSO.6 — Merge and bootstrap purge**
  - Requirements: REQ-GSO-4, REQ-GSO-6
  - Merge normally with expected head SHA and merge message containing `[storage-cleanup]`.
  - Verify storage-maintenance push run executes and succeeds.
  - Merge SHA `7832548c94514135d1908b2cb7f2c15a7665354d`; storage cleanup run `37708022649` SUCCESS; post-main CATGIS CI `37708022736` SUCCESS and CATGIS Certification `37708022676` SUCCESS.

- [-] **GSO.7 — Close storage gate**
  - Bootstrap purge verified: historical artifacts sampled from prior runs now return zero artifacts.
  - Residual successful post-main artifacts after purge were ~9.7 MB, dominated by duplicate JaCoCo/Linux report bundles.
  - Closure hardening on `maintenance/gso-zero-success-artifacts`: successful runs create zero workflow artifacts; failure diagnostics remain bounded to 7 days.
  - After PR + post-main certification, mark this spec CLOSED_CERTIFIED and return to F1.2 B1/B2.

## Current next action

Certify GSO.7 closure hardening in GitHub, merge with `[storage-cleanup]` to purge the residual ~9.7 MB, verify post-main CI/Certification/storage cleanup, then return to F1.2. Do not modify product/runtime code.
