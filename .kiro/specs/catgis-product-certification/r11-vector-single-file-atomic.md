# SPEC/Kiro #155A - fail-closed single-file vector publication

BASE: aecb4d20621bb5065f8acb388813eefe594f3a27
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN

## Scope

Protect DXF, GeoJSON, GPX, KML and KMZ outputs from direct canonical truncation. Shapefile is deliberately excluded because its multi-file family cannot honestly be made atomic by independent renames; #155B will require a recovery journal/family transaction.

## Design

- Write each single-file format to a unique same-parent CATGIS staging file with the real output extension.
- Reopen/parse the staging file with CATGIS' real DxfLoader, GeoJsonLoader, GpxLoader or KmlLoader before publication.
- For non-empty source data require reopened geometry/data; GeoJSON additionally requires exact feature count.
- Bound staging to 512 MiB, force bytes, recheck target and publish only with ATOMIC_MOVE + REPLACE_EXISTING.
- AtomicMoveNotSupported is terminal. Never fall back to non-atomic overwrite.
- Reject symlink/nonregular destination. Always clean owned staging.
- Existing refresh/reopen behavior runs only after publication succeeds.

## Evidence

VectorSingleFilePublisherContractTest injects writer failure, validator failure and AtomicMoveNotSupportedException and requires byte-identical previous target plus zero staging leftovers. It also verifies real extension preservation and directory-target rejection.

Existing ReleaseVectorInteropTest exercises production GeoJSON/DXF/KML/KMZ paths and their loaders after this wrapper. GPX loader/export coverage remains in its existing format suites and candidate certification.

## Residual

Shapefile remains destructive on protected main until #155B. Project-wide save is not transaction-safe while any mutable vector source can be partially replaced. PostGIS remains a separate database transaction contract. This tranche does not claim crash-durable directory metadata fsync or physical Windows handle behavior beyond repository runners.

Require exact-head CI/Certification/Runtime Identity/GeoParquet and authenticated predecessor-main evidence, normal expected-head merge, then fresh-main proof.


## Active legacy menu surfaces folded into this tranche
Source audit found two additional user-facing writers outside ExportVectorLayerAction:
- Layer context menu -> DxfExportEngine wrote canonical DXF directly through PrintWriter/FileOutputStream.
- Main menu -> KmlExportEngine wrote raw source coordinates directly as KML with no source CRS, which can encode projected metres as WGS84 degrees.

Current candidate centralizes both:
- DxfExportEngine writes to VectorSingleFilePublisher staging, checks PrintWriter error state, reopens with DxfLoader, then atomically publishes.
- KmlExportEngine menu delegates to ExportVectorLayerAction so source CRS is transformed to EPSG:4326 and the same staged/reopen/atomic publication is used.
- the old public feature-list-only KML writer is fail-closed because that API has no source CRS and cannot prove KML coordinate semantics.

LegacyVectorExportSafetyContractTest proves DXF loader roundtrip and projected EPSG:32719 -> KML WGS84 coordinate roundtrip.
## Current-main transplantation — 2026-10-17

Old candidate #157 HEAD `d496547e96e32887eaafd3d431a5e09e2e70b8b5` had different ancestry. Canonical main `aecb4d20621bb5065f8acb388813eefe594f3a27` was verified to have byte-identical original `DxfExportEngine`, `ExportVectorLayerAction`, `KmlExportEngine` blobs to old baseline `228e30c60c330eedf8f918d3a4ffae9e660c953d`. Therefore the three exact candidate source files, their two new bounded helpers and two failure-path tests were ported without overwriting later code. This new branch requires full independent current-base certification and post-merge push attestation. Legacy shapefile multi-file crash recovery and full project transactions remain OPEN/NOT_CERTIFIED.


## Clean port after P0 LAS integration

Previous code candidate PR #197 (`b0d2b1672c4342e509bb407fa0e15397efc88b43`) was based on pre-LAS main. Diff to new main `aecb4d20621bb5065f8acb388813eefe594f3a27` affects LAS CRS provenance classes, not DxfExportEngine, ExportVectorLayerAction or KmlExportEngine, so the exact tested source and JUnit files have been transplanted without overriding changes. This fresh candidate requires new exact-head CI, 15/15 native certification, 8/8 runtime identity, GeoParquet, and predecessor-main push attestation; former green runs are provenance only.


## Rebased after the WFS/WCS/CATMAP merge #199

BASE_SHA `aecb4d20621bb5065f8acb388813eefe594f3a27`. Historical candidate #198 HEAD `882624e55194e9dacfd4e71837df1ea894b55765` must not be merged from prior main. Exact diff of main #199 only changed WFS/WCS/UX source files, not DxfExportEngine, ExportVectorLayerAction or KmlExportEngine. Seven source/tests ported untouched into this clean base and subject to NEW CI, native Certification, Runtime Identity, GeoParquet, predecessor-main push attestation, and independent post-main proof. SHP multi-file family journaling #155 remains unresolved.
