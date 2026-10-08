# GitHub Storage Optimization — Tasks

Status: ACTIVE
Baseline: `963133c6d585cfde8bc2ea377a35af34b031321b`

- [-] **GSO.1 — Measure current storage sources**
  - Requirements: REQ-GSO-1, REQ-GSO-2, REQ-GSO-3
  - Evidence:
    - repository metadata ~10.25 MB;
    - current tree ~15.85 MB;
    - 39 Actions runs;
    - current workflow artifacts ~155.8 MB;
    - Gradle logs prove repeated cache restores/writes including ~97–130 MB shared entries and ~5 MB per-job state entries.
  - Decision: do not delete runtime-referenced manual DOCX.

- [ ] **GSO.2 — Stop new Gradle cache writes**
  - Requirement: REQ-GSO-2
  - Add `cache-disabled: true` to all active Gradle setup steps.

- [ ] **GSO.3 — Bound workflow artifact creation**
  - Requirement: REQ-GSO-3
  - Successful PR runs should not persist routine diagnostic/evidence bundles.
  - Failed jobs retain useful diagnostics.
  - Successful post-main evidence retention <= 7 days.

- [ ] **GSO.4 — Add repository storage maintenance workflow**
  - Requirements: REQ-GSO-4, REQ-GSO-5
  - Bootstrap marker: `[storage-cleanup]`.
  - Bootstrap deletes all current Actions caches and artifacts.
  - Weekly/manual maintenance deletes all caches and artifacts older than 7 days.

- [ ] **GSO.5 — PR certification**
  - Requirement: REQ-GSO-6
  - Audit exact diff.
  - Require CATGIS CI and CATGIS Certification green on exact PR head.

- [ ] **GSO.6 — Merge and bootstrap purge**
  - Requirements: REQ-GSO-4, REQ-GSO-6
  - Merge normally with expected head SHA and merge message containing `[storage-cleanup]`.
  - Verify storage-maintenance push run executes and succeeds.
  - Verify normal post-main CATGIS CI/Certification succeed.

- [ ] **GSO.7 — Close storage gate**
  - Record exact merge SHA, cleanup run and post-main runs.
  - Return to paused F1.2 B1/B2 work only after this gate is CLOSED_CERTIFIED.

## Current next action

Implement GSO.2–GSO.4 on the storage branch. Do not modify product/runtime code.
