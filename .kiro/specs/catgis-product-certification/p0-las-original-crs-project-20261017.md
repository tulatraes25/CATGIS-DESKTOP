# SPEC/Kiro P0 — LAS original-file CRS across project reprojection and reopen

**BASE_SHA:** `f5c0223be7f3f9bb4a3b8100850253026f45f059`
**STATUS:** CANDIDATE / NOT_CERTIFIED
**A0.7:** OPEN
**Tracking:** issue #193; follows PR #190

## Evidence and root cause

`LayerVectorDataSupport.ensureDataLoaded` accepts an explicitly declared
source CRS for LAS (native VLR/EVLR extraction is not yet implemented),
reads *all* declared points under the 50,000-point hard ceiling, and
projects features through `TopographyWorkflowSupport.projectVectorDataToCurrentProject`.
That projection mutates the in-memory layer `sourceCRS` to the project
target. Historically the project writer saved the mutated project CRS
as the *source CRS* of the LAS file. Reopening then reads raw LAS
coordinates (e.g. UTM EPSG:32719) but attaches a saved EPSG:4326 source
label, producing wrong geography without reliable detection.

This is a P0 correctness issue. Do not change the scientific point
coordinates, fool the projection service, invent WGS84, or relax LAS
point completeness and version integrity.

## Design

Generalize PR #190's `FlatGeobufSourceCrsProvenance` into a single
`LocalVectorSourceCrsProvenance` supporting `.fgb`, `.las` and
`.laz` file paths (LAZ remains an unsupported compressed reader).
It stores the original file-source CRS in transient layer user metadata
once, keyed by the source path, *before* projection. The in-memory
`sourceCRS` still tracks the project's projected feature collection.
`SaveProjectAction` writes the original source CRS to the existing
project-line field, not the target. On reopening a project the raw
source file is decoded and reprojected into the current target again;
the original provenance is restored by the same seam.

- No binary artifacts or changes to the persistent project format.
- No file I/O or network in the save path.
- No new geospatial libraries or untrusted EPSG defaults.
- FGB behavior and existing roundtrip contracts are preserved.
- If the layer path is changed, stale path-keyed provenance is not reused.
- Strengthen LAS's declared-source gate to reject unresolved EPSG/WKT
  immediately (before file read) while accepting valid WKT-only CRS; a
  nonblank string alone is not adequate evidence of georeferencing.
- Keep LAS limits, native tests and previously certified real FGB header
  consistency gates unchanged.

## Executable regression contract

`LasSourceCrsProjectRoundTripContractTest` generates a tiny genuine
LAS 1.2 point record with southern UTM 19S offsets. Assert actual
LAS-header and point values, real UTM→WGS84 transformed geometry,
preserved original file bytes, project-line serializer and parser
source CRS, idempotent second save/reopen, path-change invalidation,
unreferenced source rejection, and unchanged non-FGB/LAS raster behavior.
`FlatGeobufProjectCrsRoundTripContractTest` must remain green after
the shared-seam rename.

## Acceptance

Exact candidate SHA must pass CATGIS CI (including the new JUnit),
15/15 Certification jobs including real native GDAL macOS Intel,
8/8 Runtime Identity, GeoParquet, authenticated prior main push
attestation; then normal merge with expected head SHA and exact-main
postpush CI/Certification/GeoParquet/Runtime + attestation.

## Residual risk

Legacy projects already serialized with incorrectly substituted
target CRS require explicit user reconciliation, not silent overrides.
LAS/LAZ VLR/EVLR CRS decoding, scalable LAS indexing (>50,000) and
compressed LAZ support remain distinct noncertified gates.
Native GUI and scientific E2E acceptance are still pending A0.7/F3.
