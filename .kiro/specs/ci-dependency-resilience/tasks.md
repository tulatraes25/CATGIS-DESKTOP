# CI Dependency Resilience — Tasks

Status: ACTIVE
Baseline: `4da6b3036d6d59c7b4726d3b906f710215c3c269`

- [x] **CDR.1 — Add deterministic Gradle dependency resolver**
  - Requirements: REQ-CDR-2, REQ-CDR-3
  - `resolveCiDependencies` resolves every Gradle configuration with `canBeResolved == true`, including Java, Checkstyle and JaCoCo requirements.

- [x] **CDR.2 — Add bounded Linux/Windows dependency prime jobs**
  - Requirements: REQ-CDR-1, REQ-CDR-2
  - Shared keys by OS + dependency hash.
  - setup-gradle internal cache remains disabled.

- [x] **CDR.3 — Make Java/GIS jobs consume cache offline**
  - Requirements: REQ-CDR-2, REQ-CDR-3
  - Downstream cache restore must fail on miss.
  - Gradle commands run `--offline`.

- [x] **CDR.4 — Preserve bounded CATGIS dependency caches**
  - Requirement: REQ-CDR-4
  - Storage maintenance deletes unrelated caches and preserves `catgis-gradle-deps-v1-*`.

- [-] **CDR.5 — PR certification**
  - Requirement: REQ-CDR-5
  - First candidate exposed a Windows cache-path mismatch: `setup-gradle` uses `D:\\a\\.gradle`, while the cache action targeted `~/.gradle`; prime succeeded but saved no cache.
  - Corrected candidate uses the actual Windows Gradle home and must pass CATGIS CI + Certification + Storage validation on one exact head SHA.

- [ ] **CDR.6 — Merge and post-main certification**
  - Requirement: REQ-CDR-5
  - Normal merge with expected head SHA.
  - Post-main exact SHA must pass all blocking jobs.

- [ ] **CDR.7 — Resume product certification**
  - Mark this gate CLOSED_CERTIFIED.
  - Record B2a as source-complete and post-main certified only after infrastructure recovery.
  - Continue F1.2 B2b dynamic/computed command inventory.

## Current next action

Audit the CDR.1–CDR.4 diff, open the resilience PR, and require CATGIS CI + Certification + Storage validation on the exact head. Do not use OpenCode/GUI.
