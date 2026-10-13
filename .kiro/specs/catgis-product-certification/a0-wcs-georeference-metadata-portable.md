# CATGIS — SPEC/Kiro A0.4 WCS GeoTIFF georeference metadata gate

Status: DRAFT / NOT_CERTIFIED
Base protected main: `47b4b890dbb313bc33c61ebf94fb793e17d4bc8a`
Branch: `quality/a0-wcs-georeference-metadata-post54`
Related: scientific issue #43; A0.7 remains OPEN.

## Evidence / problem

Merged #53 checks staged TIFF magic, ImageIO reader and *pixel-grid size*,
but accepts any GeoTIFF that advertises the right width/height regardless of
where it is located or what CRS it encodes. A WCS response can thereby replace
an existing coverage with 16×16 cells from the wrong geographical extent or
wrong EPSG code, silently contaminating GIS analysis. The normal WcsDialog
requests explicit bbox and CRS and publishes .tif through this path.

## Requirements

- For a TIFF/GeoTIFF with an explicitly requested bbox and/or CRS, use
  GeoTools GeoTiffReader *metadata getters*, no rendered-image read and no
  pixel decode, to compare embedded CRS and geographic envelope.
- Reject missing/nonmatching GeoTIFF georeference when the caller specified it;
  preserve legitimate TIFF download behavior if neither parameter is explicit.
- Parse exactly four finite BBOX ordinates xmin,ymin,xmax,ymax with positive
  area; reject malformed requests before publishing. Define a conservative
  0.51-pixel envelope tolerance to accept pixel-centre vs edge rounding, but
  never permit an unrelated extent or projection.
- Decode requested CRS through existing CRSDefinitions, force longitude-first
  axis hints on the metadata reader, use semantic CRS comparison rather than
  comparing unnormalized WKT strings.
- Always dispose GeoTiffReader before filesystem rename/delete. On Windows an
  open TIFF file handle would prevent atomic replacement; on Linux the same
  cleanup must succeed. Reuse the existing staged-sibling + WCS fallback.
- Never add OS-specific path separators, desktop GUI tasks, huge binary fixtures,
  subprocess dependencies or a copy of raster pixels in memory.
- Do not apply TIFF georeferencing rules to explicit PNG/NetCDF/etc. response
  formats; never silently assign a fake EPSG to a missing CRS.
- Preserve successful multi-MiB 1024² TIFF streaming and 2.0.1 -> 1.0.0 fallback.

## Deterministic tests, run on both OSes

1. Valid EPSG:4326 / matching bbox with 16×16 and 1024×1024 real GeoTIFFs:
   destination bytes identical, original replaced, staging cleared.
2. Real GeoTIFF with valid TIFF structure and correct width/height but 5°
   displaced bbox: both WCS versions fail; old file remains unchanged.
3. Real GeoTIFF with correct dimensions and an EPSG:3857 geokey while caller
   requested EPSG:4326: reject both versions, preserve destination.
4. Writer and reader streams are closed before the final rename; after download
   test can move/delete the result without waiting (strong Windows contract).
5. Explicit PNG remains unaffected; missing requested bbox/crs keeps existing
   backwards-compatible structural gate.
6. CI and Certification PASS on the exact SHA, including Windows Full Suite,
   Linux Full Suite, PostGIS and architecture, followed by authenticated
   push-run attestation on exact new main merge SHA.

## Remaining scientific risks

CRS and outer envelope validation does not prove complete strip/tile pixel
decoding, correct band interpretation, NoData, compression integrity,
tiepoint/affine rotation semantics or the WCS response's scientific values.
Keep issue #43 and A0.7 OPEN. The separate main push attestation PR #52
must never be merged into the application.
