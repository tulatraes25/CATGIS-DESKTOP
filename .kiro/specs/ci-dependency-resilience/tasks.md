# CI Dependency Resilience — Tasks

Status: ACTIVE
Baseline: `4da6b3036d6d59c7b4726d3b906f710215c3c269`

- [-] **CDR.1 — Add deterministic Gradle dependency resolver**
  - Requirements: REQ-CDR-2, REQ-CDR-3
  - Add `resolveCiDependencies` resolving compile/runtime/test classpaths.

- [ ] **CDR.2 — Add bounded Linux/Windows dependency prime jobs**
  - Requirements: REQ-CDR-1, REQ-CDR-2
  - Shared keys by OS + dependency hash.
  - setup-gradle internal cache remains disabled.

- [ ] **CDR.3 — Make Java/GIS jobs consume cache offline**
  - Requirements: REQ-CDR-2, REQ-CDR-3
  - Downstream cache restore must fail on miss.
  - Gradle commands run `--offline`.

- [ ] **CDR.4 — Preserve bounded CATGIS dependency caches**
  - Requirement: REQ-CDR-4
  - Storage maintenance deletes unrelated caches and preserves `catgis-gradle-deps-v1-*`.

- [ ] **CDR.5 — PR certification**
  - Requirement: REQ-CDR-5
  - Exact head SHA; CATGIS CI + Certification + Storage validation green.

- [ ] **CDR.6 — Merge and post-main certification**
  - Requirement: REQ-CDR-5
  - Normal merge with expected head SHA.
  - Post-main exact SHA must pass all blocking jobs.

- [ ] **CDR.7 — Resume product certification**
  - Mark this gate CLOSED_CERTIFIED.
  - Record B2a as source-complete and post-main certified only after infrastructure recovery.
  - Continue F1.2 B2b dynamic/computed command inventory.

## Current next action

Implement CDR.1–CDR.4 on `maintenance/ci-osgeo-dependency-resilience`. Do not use OpenCode/GUI.
