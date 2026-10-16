# SPEC/Kiro #155A - fail-closed single-file vector publication

BASE: 48ef1490ae87ab356a0210e6efa238eae31ab8be
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
