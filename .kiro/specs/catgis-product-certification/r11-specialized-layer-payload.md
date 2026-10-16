# SPEC/Kiro #146 tranche A - specialized project layer payload integrity

BASE: 5ce112493427257093391cbb101423531dbe9f04
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
