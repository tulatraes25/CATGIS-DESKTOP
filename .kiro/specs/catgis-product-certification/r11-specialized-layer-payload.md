# SPEC/Kiro #146 tranche A - specialized project layer payload integrity

BASE: 5784f7a340ba1148aa26e0ed86d35e0749d12614
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN
ISSUE #146 REMAINS OPEN AFTER THIS TRANCHE.

## Root causes

Non-raster specialized layers write opacity at payloadStart and source fields
start at payloadStart+1. OnlineTileLayer and OnlineWmsLayer are RasterLayer
subclasses: their legacy source fields start only after the seven raster payload
slots (opacity, grayscale, autoContrast, red, green, blue, rasterMode). The
first candidate incorrectly applied the non-raster +1 rule to Tile/WMS and CI
correctly exposed `sourceId=false` instead of `sourceId=osm`. WFS, GeoPackage
and PostGIS had the opposite historical error: they started at opacity.
SpatiaLite was not reconstructed as a subtype and its model mutated the real
database path into a spatialite://...#table pseudo URI even though the loader
passes getPath() directly to java.io.File. Missing requested SpatiaLite tables
silently fell back to the first table.

## Design

New project files use keyed suffixes for Tile/WMS/WFS/GeoPackage/PostGIS/
SpatiaLite. Values may contain '='; only the project delimiter '|' is sanitized,
matching existing project format behavior. Load scans exact keys, independent of
positional offsets. If no subtype keys exist, corrected legacy positional
decoders start after non-raster opacity for WFS/GeoPackage/PostGIS and after
the complete seven-slot raster payload for Tile/WMS.

SpatiaLite now keeps the physical database path and table identity separately.
Constructor migrates legacy spatialite://<file>#<table> references. Loader fails
when the requested table is absent instead of selecting an arbitrary table.

## Tests

Package-level serialization seams roundtrip opacity and subtype fields for
Tile, WMS, WFS, GeoPackage, PostGIS and SpatiaLite, including URLs containing
'='. Legacy positional GeoPackage/PostGIS lines prove source fields start after
opacity. Legacy SpatiaLite pseudo URI migration is explicit.

## Residual

This tranche fixes document identity/metadata only. It does NOT yet stage or
attach specialized feature data during project open. Issue #144 supplies the
stage-before-commit architecture; #146 stays OPEN until local specialized
sources are reopened with real data and remote/auth sources have explicit
deferred status. No password is serialized.

Require exact-head CI/Certification/RuntimeIdentity/GeoParquet and authenticated
predecessor-main evidence before merge, then fresh main evidence.

## Failed-candidate evidence retained

Head `6fb6f69730e50f6e100e43ef0cc4751ba97abfa5` failed CI `38921538977`, Runtime Identity `38921538707` and Certification `38921538774` on the same deterministic contract: `legacyPositionalTileWmsAndWfsPayloadsStartAfterOpacity` expected tile source `osm` but decoded `false`. Root cause was the wrong +1 legacy offset for RasterLayer-derived Tile/WMS. This failure is code evidence, not infrastructure, and is not relabeled as PASS.

## Regression guard after #166 integration

During exact-main resynchronization, an older candidate SaveProjectAction version was found to replace certified ProjectFilePublisher staging with a direct FileWriter of the canonical .catgis file. This is a P0 regression and was rejected before merge. The integration now preserves the current protected-main atomic publishing call site while only replacing the specialized payload builder. ArchitectureCertificationTest guards against recurrence, and ProjectFilePublisherContractTest continues to prove byte-exact previous-file preservation and staging cleanup on write/move/validation failure.

CANDIDATE / NOT_CERTIFIED until full exact-head and post-main workflow evidence. No claim of full cross-file project transactionality or staged project open.

## New current-main candidate — 2026-10-17

The historical draft PR #147 was NOT merged because its old whole-file SaveProjectAction snapshot could have dropped certified atomic publication and newly merged original-file CRS persistence. This port is selective: `ProjectLayerPayloadCodec` and its original regression tests were copied by verified SHA, `LoadProjectAction` received only SPATIALITE subtype construction and a replacement of the legacy 8KB payload parser, while `SaveProjectAction` received only the keyed payload emission replacing its prior subtype suffix builder. Its `ProjectFilePublisher.publish` and `LocalVectorSourceCrsProvenance.crsForProjectFile` call sites remain intact. SpatiaLite model receives real physical path/table split; loader refuses missing table. The architecture test guards against FileWriter regression.

This tranche does not certify data staging for remote or SpatiaLite sources, GUI behavior, or the restoration of already incorrectly persisted legacy metadata without user review. Require full current-head 15/15 native certification, 8/8 Runtime Identity, CI, GeoParquet and authenticated predecessor-main push attestation before merge, then post-main evidence. Source changes deliberately avoid #200 SHP/GeoJSON/KML and #201 MapPanel overlaps; further recombination required if main moves.


## P0 SpatiaLite scientific CRS and direct-open truth

A deeper source audit during this same candidate confirmed two additional data-integrity bugs:
- `SpatiaLiteLoader.loadFromStore` silently assigned `EPSG:4326` to ALL spatial
  tables, regardless of their true CRS. Real projected metre coordinates could
  then be displayed as degrees. The loader must resolve CRS from the actual
  GeoTools feature schema, verify any previously/manual declared CRS against
  it, support explicitly assigned CRS when schema lacks reference, and reject
  absent or contradictory CRS instead of using WGS84.
- `OpenFileAction.openSelectedFile` constructed a `SpatiaLiteLayer`, then
  threw it away and attached a generic `VectorLayer`. That loses the selected
  table and all subtype identity before project save. The corrected direct
  import keeps the actual `SpatiaLiteLayer` with physical SQLite path,
  explicit table and schema-verified CRS. The shared original-file provenance
  also supports SpatiaLite layers across map-target projection and save/reopen.

`SpatiaLiteSourceCrsTruthContractTest` checks projected EPSG:32719 schema
truth, positive manual/WKT assignment, mismatch and missing-source failure,
and actual project-line serialize/parse/reopen with a SpatiaLite subtype and
repeated CRS preservation. No standalone SQLite binary is committed.
Existing real dependency-gated SpatiaLite and GeoPackage tests remain required
in CI; passing Java tests is NOT evidence of an interactive GUI run.

Residual: choosing from *all* available SpatiaLite tables, independently
staging their feature data before replacing the currently open project,
nonblocking EDT rehydration of saved local/remote specialized sources, and
native database integration remain separate #144/#146 acceptance gates.
Do not claim full reopen runtime until those independent requirements pass.
