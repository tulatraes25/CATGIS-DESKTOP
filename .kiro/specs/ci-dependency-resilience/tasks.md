# CI Dependency Resilience — Tasks

Status: CLOSED_CERTIFIED
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
  - Storage maintenance preserves only the two current dependency-hash keys (Linux/Windows) and deletes unrelated or stale-hash caches.

- [x] **CDR.5 — PR certification**
  - Requirement: REQ-CDR-5
  - First candidate exposed a Windows cache-path mismatch: `setup-gradle` uses `D:\\a\\.gradle`, while the cache action targeted `~/.gradle`; prime succeeded but saved no cache.
  - Corrected head `b6820731f4c49a1b6d521085099d734e2689db50` passed CATGIS CI `37771524864`, Certification `37771524791` and Storage validation `37771524810`.

- [x] **CDR.6 — Merge and post-main certification**
  - Requirement: REQ-CDR-5
  - Normal merge with expected head SHA.
  - Merge SHA `5cb0459735c5826dd315b0dda76cb8bad1d05b94`.
  - Post-main Storage Maintenance `37772233807` SUCCESS, CATGIS CI `37772233839` SUCCESS and CATGIS Certification `37772233806` SUCCESS.
  - Linux/Windows primes and all downstream offline consumers passed; no OSGeo live resolution is required after priming.

- [x] **CDR.7 — Resume product certification**
  - Mark this gate CLOSED_CERTIFIED.
  - Record B2a as source-complete and post-main certified only after infrastructure recovery.
  - Continue F1.2 B2b dynamic/computed command inventory.

## Current next action

Gate closed. Product certification resumes at F1.2 B2b on exact post-CDR main `5cb0459735c5826dd315b0dda76cb8bad1d05b94`. OpenCode/GUI remains blocked until A0.7.
