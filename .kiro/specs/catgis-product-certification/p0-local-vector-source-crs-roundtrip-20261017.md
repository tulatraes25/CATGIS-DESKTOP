# SPEC/Kiro P0 — SHP, GeoJSON and KML original-file CRS must survive project save/reopen

**BASE_SHA:** `5784f7a340ba1148aa26e0ed86d35e0749d12614`
**STATUS:** CANDIDATE / NOT_CERTIFIED
**Issue:** #195
**A0.7:** OPEN

## Root cause and user risk

The existing CATGIS `TopographyWorkflowSupport.projectVectorDataToCurrentProject`
reprojects raw source features for the display map and changes the mutable
`Layer.sourceCRS` field to the *project target*. The project serializer uses
that same field to identify the on-disk file-source CRS on reopen.
FGB (PR #190) and LAS (PR #194) now retain the original independently.
But the same issue occurs for SHP and WGS84-defined GeoJSON/KML/KMZ.

For example, a real EPSG:32719 Shapefile in an EPSG:4326 project was saved
as a source EPSG:4326 file even though its raw source coordinates remain
UTM metres. Reload would then try to interpret/project the raw metres as
geographic degrees. GeoJSON/KML can be inversely mislabeled EPSG:32719
after being displayed in a projected map.

## Minimal correction / non-goals

- Expand existing `LocalVectorSourceCrsProvenance` to `.shp`, `.geojson`,
  `.json`, `.kml`, `.kmz`, keeping `.fgb` and `.las` provenance intact.
- The existing direct-load and project-reload code paths record source
  provenance **before** reprojection, and the existing project writer
  serializes the original CRS with its existing file format (no migration).
- A valid Shapefile `.prj` can hold WKT with no resolvable EPSG code.
  In that case, derive original CRS from the real loaded schema using
  `VectorCrsTransformSupport.schemaText` and preserve WKT if necessary.
- KML and RFC7946 GeoJSON retain their defined EPSG:4326 source when
  opened, even when the current project is EPSG:32719.
- Unknown source CRS remains unknown; do not invent WGS84 for Shapefile
  with no `.prj` and no authoritative schema. The projection path
  already rejects missing CRS rather than attaching unsafe coordinates.
- Preserve path-keyed provenance invalidation: renaming the file path
  cannot reuse the old file's source code.
- No arbitrary Shapefile .prj overwrite, geometry mutation on disk,
  changes to import/export formats, native binaries, or user data.

## Exact real-file verification

`LocalVectorCrsProjectRoundTripContractTest` creates a real one-point
GeoTools Shapefile with EPSG:32719 `.prj` and point (500000, 4900000),
parses it with CATGIS `ShapefileLoader`, and checks a real GeoTools UTM
→ WGS84 longitude/latitude reprojection plus project-line serialize,
parse, second save and original .shp/.prj binary byte preservation.

It also loads real generated GeoJSON and KML WGS84 point files and proves
their source EPSG:4326 survives an EPSG:32719 displayed CRS and a
save/reopen cycle without writing the source files. Negative tests
cover blank source schema, path changes, and legacy out-of-scope formats.
All existing FGB and LAS source CRS roundtrip contracts must remain green.

## Gates / limitations

Require exact-head CATGIS CI + full 15/15 Certification including native
GDAL, 8/8 Runtime Identity, GeoParquet, authenticated previous-main push
attestation, then normal expected-head merge, fresh-main postmerge
CI/certification and authenticated attestation.

This fixes new saves and reloads only. Existing project files written with
wrong historic CRS remain a separate user-mediated reconciliation task;
do not silently rewrite them. GPX, unknown CAD/rasters, GIS GUI physical
acceptance and A0.2–A0.7 remain independently open.
