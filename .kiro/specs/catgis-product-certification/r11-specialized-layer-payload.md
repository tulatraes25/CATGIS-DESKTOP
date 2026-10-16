# SPEC/Kiro #146 tranche A - specialized project layer payload integrity

BASE: 2b915ceb118892148f4892c2d0cbb9186aefe20f
STATUS: CANDIDATE / NOT_CERTIFIED
A0.7: OPEN
ISSUE #146 REMAINS OPEN AFTER THIS TRANCHE.

## Root causes

Every non-raster layer writes opacity at payloadStart and then source-specific
fields. Legacy LoadProjectAction decoded WFS, GeoPackage and PostGIS starting at
payloadStart, thereby treating opacity as the first source field. Tile/WMS used
different +7 offsets incompatible with their serialized non-raster shape.
SpatiaLite was not reconstructed as a subtype and its model mutated the real
database path into a spatialite://...#table pseudo URI even though the loader
passes getPath() directly to java.io.File. Missing requested SpatiaLite tables
silently fell back to the first table.

## Design

New project files use keyed suffixes for Tile/WMS/WFS/GeoPackage/PostGIS/
SpatiaLite. Values may contain '='; only the project delimiter '|' is sanitized,
matching existing project format behavior. Load scans exact keys, independent of
positional offsets. If no subtype keys exist, corrected legacy positional
decoders start after non-raster opacity.

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
