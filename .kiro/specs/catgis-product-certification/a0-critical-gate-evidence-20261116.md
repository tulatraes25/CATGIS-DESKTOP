# A0 critical-gate evidence — 2026-11-16 update

Status: **A0.5 IN PROGRESS / A0.7 NOT_CERTIFIED**. This note records evidence
for the findings tracked in `a0-critical-gate-evidence-20261017.md`; it does not
certify any gate. Certification still requires the exact-SHA PR/CI/Certification/
merge/post-main attestation chain.

## Finding 1 — `.fgb`/`.las` default `sourceCRS = EPSG:4326`

- Original finding (2026-10-17): `LayerVectorDataSupport.ensureDataLoaded` set
  `sourceCRS=EPSG:4326` for `.fgb` and `.las/.laz` when the layer metadata was
  blank, so unreferenced projected coordinates could look authoritative.
- Current code: **fixed and fail-closed**.
  - FlatGeobuf: the verified header CRS is always inspected; a blank layer CRS
    requires a declared source CRS (`requireDeclaredFgbSourceCrs`) and saved CRS
    metadata is checked for consistency (`requireConsistentWithAssignedCrs`).
  - LAS/LAZ: the reader does not parse CRS VLR/EVLR, so loading requires an
    explicit declared CRS (`requireDeclaredLasSourceCrs`) and a resolvable
    authority/WKT; WGS84 is never invented.
  - GeoJSON/KML keep their format-specific WGS84 default (correct per format).
- Regression evidence (local run, this round): `FlatGeobufCrsFailClosedContractTest`,
  `LasHeaderIntegrityContractTest`, `LasSourceCrsProjectRoundTripContractTest`,
  `OpenFileFlatGeobufCrsContractTest` — **BUILD SUCCESSFUL**.
- Residual: this evidence was produced locally on Windows; it must be
  re-confirmed on the exact PR head via the normal CI/Certification chain before
  the finding is marked closed on `main`.

## Finding 2 — architecture ratchets and open A0 items

- A0.2: characterisation basis advanced by T31 (`GoldenMaster`, determinism,
  roundtrip hashes, preview==export); still pending CI certification.
- A0.4/A0.5/A0.6: remain **NOT_CERTIFIED**; the ratchet tests prevent growth but
  are not a debt-removal proof.
- A0.7/F3: **NOT_CERTIFIED** — no GUI/native runtime claim is made by this note.

## Notes

- This update changes documentation only; it does not alter code or gates.
- Evidence runs are local (`gradlew` on the working tree). The canonical gate is
  the GitHub run on the exact commit.
